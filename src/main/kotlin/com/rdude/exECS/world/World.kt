package com.rdude.exECS.world

import com.rdude.exECS.aspect.SubscriptionsManager
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.ObservableComponentChangeManager
import com.rdude.exECS.config.WorldConfiguration
import com.rdude.exECS.entity.*
import com.rdude.exECS.event.*
import com.rdude.exECS.exception.AlreadyRegisteredException
import com.rdude.exECS.exception.DefaultPoolNotExistException
import com.rdude.exECS.exception.EmptyEntityException
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
import com.rdude.exECS.utils.reflection.AutoRegistrar
import com.rdude.exECS.utils.reflection.AutoRegistrarProperties
import com.rdude.exECS.utils.systemTypeId
import kotlin.reflect.KClass

/** Core element of the exECS. Stores and manages [Events][Event], [Entities][Entity], [Components][Component] and [Systems][System].*/
class World(worldInitializer: WorldInitializer = WorldInitializer()) {

    constructor(worldInitializer: WorldInitializer.() -> Unit) : this(WorldInitializer().apply(worldInitializer))


    @JvmField val configuration = WorldConfiguration(this, worldInitializer)

    @JvmField internal val subscriptionsManager: SubscriptionsManager

    @JvmField internal val entityMapper: EntityMapper

    init {
        val freshRemovedEntitiesArray = IntArrayStackSet()
        val freshAddedEntitiesArray = IntIterableArray()
        subscriptionsManager = SubscriptionsManager(this, freshAddedEntitiesArray, freshRemovedEntitiesArray, worldInitializer.entityCapacity)
        entityMapper = EntityMapper(this, freshAddedEntitiesArray, freshRemovedEntitiesArray, worldInitializer.entityCapacity)
    }

    @JvmField internal val systems = Array<System?>(ExEcs.systemTypeIDsResolver.size) { null }

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

    @JvmField val utils = WorldUtils(this)


    /** Queues [ActingEvent], fires all queued [Events][Event].*/
    fun act() {
        isCurrentlyActing = true
        // changes could have occurred outside of act method call, in which case subscriptions should be updated first.
        updateSubscriptions()
        eventBus.queueEvent(ActingEvent, configuration.actingEventPriority)
        eventBus.fireEvents()
        if (internalChangeOccurred) {
            updateSubscriptions()
            entityMapper.removeRequested()
            poolablesManager.returnPoolablesToPoolIfNeeded()
            internalChangeOccurred = false
        }
        isCurrentlyActing = false
    }


    /** Queues the given [event].*/
    fun queueEvent(event: Event, priority: EventPriority = event.defaultPriority()) = eventBus.queueEvent(event, priority)


    /** Obtains an [Event] of type [T] from the default Pool and queues it.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist*/
    inline fun <reified T> queueEvent() where T : Event, T : Poolable = queueEvent(fromPool<T>())


    /** Obtains an [Event] of type [T] from the default Pool and queues it.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist*/
    inline fun <reified T> queueEvent(priority: EventPriority) where T : Event, T : Poolable = queueEvent(fromPool<T>(), priority)


    /** Obtains an [Event] of type [T] from the default Pool, applies [apply] function to it and queues this Event.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist */
    inline fun <reified T> queueEvent(apply: T.() -> Unit) where T : Event, T : Poolable =
        queueEvent(fromPool<T>().apply { apply.invoke(this) })


    /** Obtains an [Event] of type [T] from the default Pool, applies [apply] function to it and queues this Event.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist */
    inline fun <reified T> queueEvent(priority: EventPriority, apply: T.() -> Unit) where T : Event, T : Poolable =
        queueEvent(fromPool<T>().apply { apply.invoke(this) }, priority)


    /** Creates an [Entity] with given components. At least one component must be passed to the arguments.
     * Queues [EntityAddedEvent].
     * @return created [Entity].
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntity(components: Iterable<Component>): Entity {
        components.firstOrNull() ?: throw EmptyEntityException()
        return entityMapper.create(components)
    }


    /** Creates an [Entity] with given components. At least one component must be passed to the arguments.
     * Queues [EntityAddedEvent].
     * @return created [Entity].
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntity(vararg components: Component): Entity {
        if (components.isEmpty()) throw EmptyEntityException()
        return entityMapper.create(components)
    }


    /** Creates an [Entity] from the given [EntityBlueprint]. Queues [EntityAddedEvent].
     * @return created [Entity]*/
    @Suppress("UNCHECKED_CAST")
    fun <T : EntityBlueprintConfiguration> createEntity(entityBlueprint: EntityBlueprint<T>): Entity {
        val builder = EntityBuilder.pool.obtain() as EntityBuilder<T>
        val id = entityBlueprint.fullBuildingFun.invoke(this, builder, entityBlueprint.defaultConfiguration.invoke())
        builder.returnToPool()
        return Entity(id)
    }


    /** Creates an [Entity] from the given [EntityBlueprint] with applied [configuration]. Queues [EntityAddedEvent].
     * @return created [Entity]*/
    @Suppress("UNCHECKED_CAST")
    inline fun <T : EntityBlueprintConfiguration> createEntity(
        entityBlueprint: EntityBlueprint<T>,
        configuration: T.() -> Unit
    ): Entity {
        val builder = EntityBuilder.pool.obtain() as EntityBuilder<T>
        val config = entityBlueprint.defaultConfiguration.invoke().apply { configuration() }
        val id = entityBlueprint.fullBuildingFun.invoke(this, builder, config)
        builder.returnToPool()
        return Entity(id)
    }


    /** Creates the specified amount of Entities that will share the given [Component]s.
     * Queues [EntityAddedEvent] for each added [Entity].
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntitiesWithSameComponents(amount: Int, vararg components: Component) {
        for (i in 0 until amount) {
            createEntity(*components)
        }
    }


    /** Creates the specified amount of Entities that will share the given [Component]s.
     * Queues [EntityAddedEvent] for each added [Entity].
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntitiesWithSameComponents(amount: Int, components: Iterable<Component>) {
        for (i in 0 until amount) {
            createEntity(components)
        }
    }


    /** Creates the specified amount of [Entities][Entity] with supplied [components].
     * Queues [EntityAddedEvent] for each added Entity.
     * @throws [EmptyEntityException] if no components have been passed */
    fun createEntities(amount: Int, vararg components: (Int) -> Component) {
        for (i in 0 until amount) {
            createEntity(*Array(components.size) { components[it](i) })
        }
    }


    /** Creates the specified amount of [Entities][Entity] with supplied [components].
     * Queues [EntityAddedEvent] for each added Entity.
     * @throws [EmptyEntityException] if no components have been passed */
    fun createEntities(amount: Int, components: Iterable<(Int) -> Component>) {
        createEntity(components.mapIndexed { index, function -> function(index) })
    }


    /** Adds [singletonEntity] to this World. Queues [EntityAddedEvent].
     * @throws [AlreadyRegisteredException] if [SingletonEntity] of the same type is already registered in this World.*/
    fun addSingletonEntity(singletonEntity: SingletonEntity) {
        if (singletonEntity.world != null) throw AlreadyRegisteredException(singletonEntity)
        singletonEntity.world = this
        entityMapper.addSingletonEntity(singletonEntity)
    }


    /** Auto registers [SingletonEntities][SingletonEntity] from all packages.*/
    fun autoRegisterSingletonEntities() =
        AutoRegistrar<SingletonEntity, World> { s, w -> w.addSingletonEntity(s) }
            .register(this)


    /** Auto registers [SingletonEntities][SingletonEntity] from all packages using configured registrar.*/
    fun autoRegisterSingletonEntities(apply: AutoRegistrarProperties<SingletonEntity, World>.() -> Unit) {
        val registrar = AutoRegistrar<SingletonEntity, World> { s, w -> w.addSingletonEntity(s) }
        registrar.apply()
        registrar.register(this)
    }


    /** Auto registers [SingletonEntities][SingletonEntity] from all packages using provided registrar.*/
    fun autoRegisterSingletonEntities(registrar: AutoRegistrar<SingletonEntity, World>) {
        registrar.register(this)
    }


    /** @return [SingletonEntity] of type [T] or null if Singleton with this type is not registered in this World.*/
    fun <T : SingletonEntity> getSingletonEntity(cl: KClass<T>): T? =
        entityMapper.singletons[ExEcs.singletonEntityIDsResolver.idFor(cl)] as T?


    /** @return [SingletonEntity] of type [T] or null if Singleton with this type is not registered in this World.*/
    inline fun <reified T : SingletonEntity> getSingletonEntity(): T? = getSingletonEntity(T::class)


    /** Register [system] in this World.
     * @throws [AlreadyRegisteredException] if System of the same type is already registered in this World.*/
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


    /** Auto registers [Systems][System] from all packages.*/
    fun autoRegisterSystems() = //SystemsAutoRegistrar.defaultRegistrar.register(this)
        AutoRegistrar<System, World> { s, w -> w.registerSystem(s) }
            .register(this)


    /** Auto registers [Systems][System] from all packages using configured registrar.*/
    fun autoRegisterSystems(apply: AutoRegistrarProperties<System, World>.() -> Unit) {
        val registrar = AutoRegistrar<System, World> { s, w -> w.registerSystem(s) }
        registrar.apply()
        registrar.register(this)
    }


    /** Auto registers [Systems][System] from all packages using provided registrar.*/
    fun autoRegisterSystems(registrar: AutoRegistrar<System, World>) {
        registrar.register(this)
    }


    /** Removes a System of the given type from this World.
     * @return true if this World contained a System of the specified type
     * @throws [IllegalStateException] if this method is called during the execution of the [act] method.*/
    fun removeSystem(ofType: KClass<out System>): Boolean = removeSystem(ofType.systemTypeId)


    /** Removes a System of the given type from this World.
     * @return true if this World contained a System of the specified type
     * @throws [IllegalStateException] if this method is called during the execution of the [act] method. */
    inline fun <reified T : System> removeSystem(): Boolean = removeSystem(T::class)


    private fun removeSystem(typeId: Int): Boolean {
        if (isCurrentlyActing) throw IllegalStateException("System can not be removed during act method execution")
        val system = systems[typeId]  ?: return false
        systems[typeId] = null
        if (system is EventSystem<*>) {
            eventBus.removeSystem(system)
            internalEventsFiringManager.removeSystem(system)
        }
        system.world = null
        // update generated fields based on the current world
        ExEcs.generatedFieldsInitializer.systemRemoved(system, this)
        return true
    }


    /** @return [System] of type [T] or null if System with this type is not registered in this World.*/
    @Suppress("UNCHECKED_CAST")
    fun <T : System> getSystem(ofType: KClass<T>): T? = systems[ofType.systemTypeId] as T?


    /** @return [System] of type [T] or null if System with this type is not registered in this World. */
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


    /** Generates a [WorldSnapshot] using provided [generator].*/
    fun <T : WorldSnapshot> snapshot(generator: WorldSnapshotGenerator<T>): T  {
        rearrange()
        return generator.generate(this)
    }


    /** Generates a [SimpleWorldSnapshot].*/
    fun snapshot(): SimpleWorldSnapshot = SimpleWorldSnapshotGenerator.generate(this)


    /** Populates this World with data stored in the [snapshot] using provided [generator].*/
    fun <T : WorldSnapshot> fromSnapshot(snapshot: T, generator: WorldSnapshotGenerator<T>) =
        generator.snapshotToWorld(snapshot, this)


    /** Populates this World with data stored in the [snapshot].*/
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