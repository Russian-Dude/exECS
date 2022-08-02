package com.rdude.exECS.world

import com.rdude.exECS.aspect.SubscriptionsManager
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.ObservableComponentChangeManager
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.*
import com.rdude.exECS.exception.EmptyEntityException
import com.rdude.exECS.exception.AlreadyRegisteredException
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.PoolablesManager
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.serialization.SimpleWorldSnapshot
import com.rdude.exECS.serialization.SimpleWorldSnapshotGenerator
import com.rdude.exECS.serialization.WorldSnapshot
import com.rdude.exECS.serialization.WorldSnapshotGenerator
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.system.IterableEventSystem
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ComponentTypeToEntityPair
import com.rdude.exECS.utils.collections.IntArrayStackSet
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.systemTypeId
import kotlin.reflect.KClass

class World {

    @JvmField internal val subscriptionsManager: SubscriptionsManager

    @JvmField internal val entityMapper: EntityMapper

    init {
        val freshRemovedEntitiesArray = IntArrayStackSet()
        val freshAddedEntitiesArray = IntIterableArray()
        subscriptionsManager = SubscriptionsManager(this, freshAddedEntitiesArray, freshRemovedEntitiesArray)
        entityMapper = EntityMapper(this, freshAddedEntitiesArray, freshRemovedEntitiesArray)
    }

    @JvmField internal val systems = Array<System?>(ExEcs.systemTypeIDsResolver.size) { null }

    @JvmField internal val actingEvent = ActingEvent(0.0)

    @JvmField internal val eventBus = EventBus(this)

    @JvmField internal val internalEventsFiringManager = InternalEventsFiringManager(this)

    @JvmField internal val observableComponentChangeManager = ObservableComponentChangeManager(this)

    @JvmField internal val poolablesManager = PoolablesManager()

    internal var internalChangeOccurred = false
        set(value) {
            field = value
            if (value) subscriptionsManager.updateRequired = true
        }

    var isCurrentlyActing = false
        private set


    fun act(delta: Double) {
        isCurrentlyActing = true
        // changes could have occurred outside of act method call, in which case subscriptions should be updated first.
        updateSubscriptions()
        actingEvent.delta = delta
        eventBus.queueEvent(actingEvent)
        eventBus.fireEvents()
        if (internalChangeOccurred) {
            updateSubscriptions()
            entityMapper.removeRequested()
            poolablesManager.returnPoolablesToPoolIfNeeded()
            internalChangeOccurred = false
        }
        isCurrentlyActing = false
    }

    fun queueEvent(event: Event) = eventBus.queueEvent(event)

    inline fun <reified T> queueEvent() where T : Event, T : Poolable = queueEvent(fromPool<T>())

    inline fun <reified T> queueEvent(apply: T.() -> Unit) where T : Event, T : Poolable =
        queueEvent(fromPool<T>().apply { apply.invoke(this) })

    /** Creates an Entity with given components. At least one component must be passed to the arguments.
     * @return created [Entity].
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntity(components: Iterable<Component>): Entity {
        components.firstOrNull() ?: throw EmptyEntityException()
        return entityMapper.create(components)
    }

    /** Creates an Entity with given components. At least one component must be passed to the arguments.
     * @return created [Entity].
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntity(vararg components: Component): Entity {
        if (components.isEmpty()) throw EmptyEntityException()
        return entityMapper.create(components)
    }


    /** Creates the specified amount of Entities that will share the given [Component]s.
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntitiesWithSameComponents(amount: Int, vararg components: Component) {
        for (i in 0 until amount) {
            createEntity(*components)
        }
    }

    fun createEntitiesWithSameComponents(amount: Int, components: Iterable<Component>) {
        for (i in 0 until amount) {
            createEntity(components)
        }
    }

    fun createEntities(amount: Int, vararg components: (Int) -> Component) {
        for (i in 0 until amount) {
            createEntity(*Array(components.size) { components[it](i) })
        }
    }

    fun createEntities(amount: Int, components: Iterable<(Int) -> Component>) {
        createEntity(components.mapIndexed { index, function -> function(index) })
    }

    fun addSingletonEntity(singletonEntity: SingletonEntity) {
        if (singletonEntity.world != null) throw AlreadyRegisteredException(singletonEntity)
        singletonEntity.world = this
        entityMapper.addSingletonEntity(singletonEntity)
    }

    fun <T : SingletonEntity> getSingletonEntity(cl: KClass<T>): T? =
        entityMapper.singletons[ExEcs.singletonEntityIDsResolver.idFor(cl)] as T?

    inline fun <reified T : SingletonEntity> getSingletonEntity(): T? = getSingletonEntity(T::class)

    fun registerSystem(system: System) {
        if (systems[system.typeId] != null) throw AlreadyRegisteredException(system)
        systems[system.typeId] = system
        system.world = this
        if (system is IterableEventSystem<*>) {
            subscriptionsManager.registerSystem(system)
        }
        if (system is EventSystem<*>) {
            eventBus.registerSystem(system)
            internalEventsFiringManager.registerSystem(system)
        }
        // update generated fields based on the current world
        ExEcs.generatedFieldsInitializer.systemAdded(system, this)
    }

    fun removeSystem(system: System) = removeSystem(system.typeId)

    fun removeSystem(ofType: KClass<out System>) = removeSystem(ofType.systemTypeId)

    inline fun <reified T : System> removeSystem() = removeSystem(T::class)

    private fun removeSystem(typeId: Int) {
        if (isCurrentlyActing) throw IllegalStateException("System can not be removed during act method execution")
        val system = systems[typeId]  ?: throw IllegalArgumentException("System can not be removed as it is not registered in the World")
        systems[typeId] = null
        if (system is EventSystem<*>) {
            eventBus.removeSystem(system)
            internalEventsFiringManager.removeSystem(system)
        }
        system.world = null
        // update generated fields based on the current world
        ExEcs.generatedFieldsInitializer.systemRemoved(system, this)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : System> getSystem(ofType: KClass<T>): T? = systems[ofType.systemTypeId] as T?

    inline fun <reified T : System> getSystem(): T? = getSystem(T::class)

    /** Removes all [Entities][Entity] from this World. [EntityRemovedEvents][EntityRemovedEvent] will not be queued.*/
    fun clearEntities() {
        entityMapper.clear()
        subscriptionsManager.unsubscribeAll()
    }

    /** Rearrange entity IDs to eliminate potential gaps that could be caused by unused IDs.
     * @throws [IllegalStateException] if called during the [act] method execution.*/
    fun rearrange() {
        if (isCurrentlyActing) throw IllegalStateException("World can not be rearranged while it is acting")
        updateSubscriptions()
        entityMapper.removeRequested()
        entityMapper.rearrange()
    }

    fun <T : WorldSnapshot> snapshot(generator: WorldSnapshotGenerator<T>): T  {
        rearrange()
        return generator.generate(this)
    }

    fun snapshot(): SimpleWorldSnapshot = SimpleWorldSnapshotGenerator.generate(this)

    fun <T : WorldSnapshot> fromSnapshot(snapshot: T, generator: WorldSnapshotGenerator<T>) =
        generator.snapshotToWorld(snapshot, this)

    fun fromSnapshot(snapshot: SimpleWorldSnapshot) = SimpleWorldSnapshotGenerator.snapshotToWorld(snapshot, this)

    internal fun updateSubscriptions() = subscriptionsManager.updateSubscriptions()

    internal fun componentPresenceChange(change: ComponentTypeToEntityPair) =
        subscriptionsManager.componentChanged(change)

    internal fun <T : ComponentChange> componentChanged(component: ObservableComponent<T>, change: T) =
        observableComponentChangeManager.componentChanged(component, change)

    internal fun requestRemoveEntity(id: Int) = entityMapper.requestRemove(id)

    companion object {
        operator fun invoke(simpleWorldSnapshot: SimpleWorldSnapshot) =
            SimpleWorldSnapshotGenerator.snapshotToNewWorld(simpleWorldSnapshot)
    }

}