package com.rdude.exECS.world

import com.rdude.exECS.aspect.SubscriptionsManager
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.ObservableComponentChangeManager
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.ActingEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.event.EventBus
import com.rdude.exECS.exception.EmptyEntityException
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.PoolablesManager
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.serialization.SimpleWorldSnapshot
import com.rdude.exECS.serialization.SimpleWorldSnapshotGenerator
import com.rdude.exECS.serialization.WorldSnapshot
import com.rdude.exECS.serialization.WorldSnapshotGenerator
import com.rdude.exECS.system.*
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ComponentTypeToEntityPair
import com.rdude.exECS.utils.collections.IntArrayStackSet
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import kotlin.reflect.KClass

class World {

    init {
        ExEcs.initializeIfNeeded()
    }

    @JvmField internal val subscriptionsManager: SubscriptionsManager

    @JvmField internal val entityMapper: EntityMapper

    init {
        val freshRemovedEntitiesArray = IntArrayStackSet()
        val freshAddedEntitiesArray = IntIterableArray()
        subscriptionsManager = SubscriptionsManager(this, freshAddedEntitiesArray, freshRemovedEntitiesArray)
        entityMapper = EntityMapper(this, freshAddedEntitiesArray, freshRemovedEntitiesArray)
    }

    @JvmField internal val systems = IterableArray<System>()

    @JvmField internal val actingEvent = ActingEvent(0.0)

    @JvmField internal val eventBus = EventBus(this)

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
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntity(components: Iterable<Component>) {
        components.firstOrNull() ?: throw EmptyEntityException()
        entityMapper.create(components)
    }

    /** Creates an Entity with given components. At least one component must be passed to the arguments.
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntity(vararg components: Component) {
        if (components.isEmpty()) throw EmptyEntityException()
        entityMapper.create(components)
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
        if (singletonEntity.isWorldInitialized && singletonEntity.world != this) {
            throw IllegalStateException("Singleton entity instance can only be registered in one World instance")
        }
        if (!singletonEntity.isWorldInitialized) {
            singletonEntity.setWorld(this)
        }
        entityMapper.addSingletonEntity(singletonEntity)
    }

    fun <T : SingletonEntity> getEntitySingleton(cl: KClass<T>): T? =
        entityMapper.singletons[ExEcs.singletonEntityIDsResolver.getId(cl)] as T?

    inline fun <reified T : SingletonEntity> getEntitySingleton(): T? = getEntitySingleton(T::class)

    fun addSystem(system: System) {
        checkSystemCorrectness(system)
        systems.add(system)
        system.world = this
        subscriptionsManager.registerSystem(system)
        if (system is EventSystem<*>) {
            eventBus.registerSystem(system)
        }
    }

    fun removeSystem(system: System) {
        if (system.world != this) return
        if (isCurrentlyActing) throw IllegalStateException("System can not be removed during act method execution")
        systems.removeContainingOrder(system)
        if (system is ActingSystem) {
            eventBus.removeSystem(system)
        }
        system.world = null
    }

    fun removeSystem(ofType: KClass<out System>) =
        systems.firstOrNull { it::class == ofType }
            ?.let { removeSystem(it) }

    inline fun <reified T : System> removeSystem() = removeSystem(T::class)

    fun <T : System> getSystem(ofType: KClass<T>): T? = systems.firstOrNull { it::class == ofType } as T?

    inline fun <reified T : System> getSystem(): T? = getSystem(T::class)

    /** Removes all entities. [EntityRemovedEvent]s will not be called.*/
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

    internal fun updateSubscriptions() = subscriptionsManager.updateSubscriptions()

    internal fun componentPresenceChange(change: ComponentTypeToEntityPair) =
        subscriptionsManager.componentChanged(change)

    internal fun <T : ComponentChange> componentChanged(component: ObservableComponent<T>, change: T) =
        observableComponentChangeManager.componentChanged(component, change)

    internal fun requestRemoveEntity(id: Int) = entityMapper.requestRemove(id)

    private fun checkSystemCorrectness(system: System) {
        if (system.world != null) {
            throw IllegalStateException("System $system is already registered in another world")
        }
        if (
            system is EventSystem<*>
            && system.aspect.anyOf.isEmpty() && system.aspect.allOf.isEmpty()
            && !(system is SimpleActingSystem || system is SimpleEventSystem<*>)
        ) {
            val usedName = if (system is ActingSystem) ActingSystem::class.simpleName else EventSystem::class.simpleName
            val needName =
                if (system is ActingSystem) SimpleActingSystem::class.simpleName else SimpleEventSystem::class.simpleName
            throw IllegalStateException(
                "System ${system::class} has no components in aspect. To use $usedName without components in aspect, extend from $needName instead of $usedName"
            )
        }
    }

    companion object {
        operator fun invoke(simpleWorldSnapshot: SimpleWorldSnapshot) =
            SimpleWorldSnapshotGenerator.snapshotToWorld(simpleWorldSnapshot)
    }

}