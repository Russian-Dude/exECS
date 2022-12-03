package com.rdude.exECS.world

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.*
import com.rdude.exECS.event.*
import com.rdude.exECS.exception.*
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.collections.EntitiesSet
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.utils.singletonTypeId
import kotlin.reflect.KClass

/** Provides access to the [World] and [Entity] methods directly from the inheritors of this class.
 * Calls to the methods declared in this class will be optimized by the exECS [compiler plugin](https://github.com/Russian-Dude/execs-plugin).*/
abstract class WorldAccessor {

    abstract val world: World?


    /** Creates an [Entity] with the given [components]. At least one component must be passed to the arguments.
     * Queues [EntityAddedEvent].
     * @return created [Entity]
     * @throws [EmptyEntityException] if no components have been passed
     * @throws [WorldNotSetException] if [world] property is null*/
    protected fun createEntity(vararg components: Component) = world?.createEntity(*components) ?: throw WorldNotSetException(this)


    /** Creates an [Entity] with the given [components]. At least one component must be passed to the arguments.
     * Queues [EntityAddedEvent].
     * @return created [Entity]
     * @throws [EmptyEntityException] if no components have been passed
     * @throws [WorldNotSetException] if [world] property is null*/
    protected fun createEntity(components: Iterable<Component>) = world?.createEntity(components) ?: throw WorldNotSetException(this)


    /** Creates an [Entity] from the given [EntityBlueprint]. Queues [EntityAddedEvent].
     * @return created [Entity]
     * @throws [WorldNotSetException] if [world] property is null*/
    fun createEntity(entityBlueprint: EntityBlueprint<*>): Entity =
        world?.createEntity(entityBlueprint) ?: throw WorldNotSetException(this)


    /** Creates an [Entity] from the given [EntityBlueprint] with applied [configuration]. Queues [EntityAddedEvent].
     * @return created [Entity]*/
    inline fun <T : EntityBlueprintConfiguration> createEntity(
        entityBlueprint: EntityBlueprint<T>,
        configuration: T.() -> Unit
    ): Entity = world?.createEntity(entityBlueprint, configuration) ?: throw WorldNotSetException(this)


    /** Creates the specified amount of [Entities][Entity] that will share the given [components].
     * Queues [EntityAddedEvent] for each added Entity.
     * @throws [EmptyEntityException] if no components have been passed
     * @throws [WorldNotSetException] if [world] property is null*/
     protected fun createEntitiesWithSameComponents(amount: Int, vararg components: Component) =
        world?.createEntitiesWithSameComponents(amount, *components) ?: throw WorldNotSetException(this)


    /** Creates the specified amount of [Entities][Entity] that will share the given [components].
     * Queues [EntityAddedEvent] for each added Entity.
     * @throws [EmptyEntityException] if no components have been passed
     * @throws [WorldNotSetException] if [world] property is null*/
    protected fun createEntitiesWithSameComponents(amount: Int, components: Iterable<Component>) =
        world?.createEntitiesWithSameComponents(amount, components) ?: throw WorldNotSetException(this)


    /** Creates the specified amount of [Entities][Entity] with supplied [components].
     * Queues [EntityAddedEvent] for each added Entity.
     * @throws [EmptyEntityException] if no components have been passed
     * @throws [WorldNotSetException] if [world] property is null*/
     protected fun createEntities(amount: Int, vararg components: (Int) -> Component) =
        world?.createEntities(amount, *components) ?: throw WorldNotSetException(this)


    /** Creates the specified amount of [Entities][Entity] with supplied [components].
     * Queues [EntityAddedEvent] for each added Entity.
     * @throws [EmptyEntityException] if no components have been passed
     * @throws [WorldNotSetException] if [world] property is null*/
    protected fun createEntities(amount: Int, components: Iterable<(Int) -> Component>) =
        world?.createEntities(amount, components) ?: throw WorldNotSetException(this)


    /** Queues the given [event].
     * @throws [WorldNotSetException] if [world] property is null*/
     protected fun queueEvent(event: Event, priority: EventPriority = event.defaultPriority()) =
        world?.queueEvent(event, priority) ?: throw WorldNotSetException(this)


    /** Obtains an [Event] of type [T] from the default Pool and queues it.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T> queueEvent() where T : Event, T : Poolable =
        world?.queueEvent<T>() ?: throw WorldNotSetException(this)


    /** Obtains an [Event] of type [T] from the default Pool and queues it.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T> queueEvent(priority: EventPriority) where T : Event, T : Poolable =
        world?.queueEvent<T>(priority) ?: throw WorldNotSetException(this)


    /** Obtains an [Event] of type [T] from the default Pool, applies [apply] function to it and queues this Event.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T> queueEvent(apply: T.() -> Unit) where T : Event, T : Poolable =
        world?.queueEvent(apply) ?: throw WorldNotSetException(this)


    /** Obtains an [Event] of type [T] from the default Pool, applies [apply] function to it and queues this Event.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T> queueEvent(priority: EventPriority, apply: T.() -> Unit) where T : Event, T : Poolable =
        world?.queueEvent(priority, apply) ?: throw WorldNotSetException(this)


    /** @return [SingletonEntity] of type [T] or null if Singleton with this type is not registered in the [world].
     * @throws [WorldNotSetException] if [world] property is null*/
    @Suppress("UNCHECKED_CAST")
    protected fun <T : SingletonEntity> getSingletonEntity(cl: KClass<T>): T? =
        (world ?: throw WorldNotSetException(this))
            .entityMapper.singletons[cl.singletonTypeId] as T?


    /** @return [SingletonEntity] of type [T] or null if Singleton with this type is not registered in the [world].
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T : SingletonEntity> getSingletonEntity(): T? = getSingletonEntity(T::class)


    /** Adds [singletonEntity] to the [world]. Queues [EntityAddedEvent].
     * @throws [AlreadyRegisteredException] if [SingletonEntity] of the same type is already registered in the [world]
     * @throws [WorldNotSetException] if [world] property is null*/
    protected fun <T : SingletonEntity> addSingletonEntity(singletonEntity: T) =
        (world ?: throw WorldNotSetException(this))
            .addSingletonEntity(singletonEntity)


    /** @return [System] of type [T] or null if System with this type is not registered in the [world].
     * @throws [WorldNotSetException] if [world] property is null*/
    protected fun <T : System> getSystem(systemType: KClass<T>): T? =
        (world ?: throw WorldNotSetException(this))
            .getSystem(systemType)


    /** @return [System] of type [T] or null if System with this type is not registered in the [world].
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T : System> getSystem(): T? =
        (world ?: throw WorldNotSetException(this))
            .getSystem(T::class)


    /** @return [Component] of type [T] or null if this Entity does not have component of such type.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Component> Entity.getComponent(componentClass: KClass<T>): T? =
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[componentClass.componentTypeId][id] as T?


    /** @return [Component] of type [T] or null if this Entity does not have component of such type.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T : Component> Entity.getComponent(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this Entity does not have component of such type.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline operator fun <reified T : Component> Entity.invoke(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this Entity does not have component of such type.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun <T : Component> Entity.get(componentClass: KClass<T>): T? = getComponent(componentClass)


    /** Removes [Component] of type [T] from this Entity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun <T : Component> Entity.removeComponent(componentClass: KClass<T>) {
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[componentClass.componentTypeId].removeComponent(id)
    }


    /** Removes [Component] of type [T] from this Entity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T : Component> Entity.removeComponent() = removeComponent(T::class)


    /** Removes [Component] of type [T] from this Entity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun <T : Component> Entity.minusAssign(componentClass: KClass<T>) = removeComponent(componentClass)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this Entity has a [Component] of type [T].
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun <T : Component> Entity.hasComponent(componentClass: KClass<T>): Boolean =
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[componentClass.componentTypeId].hasComponent(id)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this Entity has a [Component] of type [T].
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T : Component> Entity.hasComponent() = hasComponent(T::class)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this Entity has a [Component] of type [T].
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun <T : Component> Entity.contains(componentClass: KClass<T>) = hasComponent(componentClass)


    /** Adds [component] to this Entity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.addComponent(component: Component) =
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[component.getComponentTypeId()].addComponentUnsafe(id, component)


    /** Adds [component] to this Entity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun Entity.plusAssign(component: Component) = addComponent(component)


    /** Obtains a [Component] of type [T] from the default Pool and adds it to this Entity.
     * Queues [ComponentAddedEvent]  if Component has been added.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T> Entity.addComponent() where T : Component, T : Poolable =
        addComponent(fromPool<T>())


    /** Obtains [Component] of type [T] from the default Pool, apply [apply] function to this [Component] and adds it
     * to this Entity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T> Entity.addComponent(apply: T.() -> Unit) where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
    }


    /** Removes this Entity from the [World]. Queues [EntityRemovedEvent].
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.remove() = (world ?: throw WorldNotSetException(this@WorldAccessor)).requestRemoveEntity(id)


    /** Removes this [Entity] from the parent Entity if this Entity is a child.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.removeFromParent() {
        val entityMapper = world?.entityMapper ?: throw WorldNotSetException(this@WorldAccessor)
        val childComponent = entityMapper.childEntityComponents[this.id] ?: return
        entityMapper.removeChildEntity(childComponent.parentEntityId, this.id)
    }


    /** Removes child [entity] from this [Entity] if [entity] is a child of this Entity.
     * @throws [NoEntityException] if this Entity or [entity] argument is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.removeChild(entity: Entity) {
        val entityMapper = world?.entityMapper ?: throw WorldNotSetException(this@WorldAccessor)
        val childComponent = entityMapper.childEntityComponents[entity.id] ?: return
        if (childComponent.parent.id != this.id) return
        entityMapper.removeChildEntity(this.id, entity.id)
    }


    /** Removes child [entity] from this [Entity] if [entity] is a child of this Entity.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.removeChild(entity: SingletonEntity) = removeChild(entity.asEntity())


    /** Adds [entity] as a child to this [Entity]. If [entity] is already a child of another Entity, switches parent.
     * @throws [NoEntityException] if this Entity or [entity] argument is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.addChild(entity: Entity) {
        val entityMapper = world?.entityMapper ?: throw WorldNotSetException(this@WorldAccessor)
        entityMapper.addChildEntity(this.id, entity.id)
    }


    /** Adds [entity] as a child to this [Entity]. If [entity] is already a child of another Entity, switches parent.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.addChild(entity: SingletonEntity) = addChild(entity.asEntity())


    /** Child Entities of this [Entity].
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected val Entity.children: EntitiesSet get() {
        val entityMapper = world?.entityMapper ?: throw WorldNotSetException(this@WorldAccessor)
        return entityMapper.parentEntityComponents[this.id]?.children ?: EntitiesSet.EMPTY_SET
    }


    /** Parent [Entity] of this Entity or [Entity.NO_ENTITY] if it does not have a parent.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected val Entity.parent: Entity get() {
        val entityMapper = world?.entityMapper ?: throw WorldNotSetException(this@WorldAccessor)
        return entityMapper.childEntityComponents[this.id]?.parent ?: Entity.NO_ENTITY
    }


    /** Generates a List of [Components][Component] plugged into this Entity.
     * @throws [NoEntityException] if this Entity is [Entity.NO_ENTITY]
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.generateComponentsList(): List<Component> =
        if (this.id < 0) throw NoEntityException("Can not generate Components list for Entity.NO_ENTITY")
        else world?.entityMapper?.componentMappers?.mapNotNull { it[id] } ?: throw WorldNotSetException(this@WorldAccessor)


    // generating here because access to the entity methods is required
    internal fun generateEntityOrder(definition: EntityOrder.Definition): EntityOrder =
        when (definition) {
            is EntityOrder.Definition.NotSpecified -> EntityOrder.NOT_SPECIFIED
            is EntityOrder.Definition.By<*> -> generateEntityOrderBy(definition)
            is EntityOrder.Definition.Having -> generateEntityOrderHaving(definition)
            // Exception should never occur
            is EntityOrder.Definition.Custom -> throw RuntimeException("Custom EntityOrder can not be generated and must be specified manually")
        }

    private fun generateEntityOrderHaving(definition: EntityOrder.Definition.Having): EntityOrder =
        with(definition) {
            if (nullsFirst) EntityOrder(definition, cl) { e1, e2 ->
                val c1 = e1.getComponent(cl)
                val c2 = e2.getComponent(cl)
                if (c1 == null) {
                    if (c2 == null) 0
                    else -1
                } else {
                    if (c2 == null) 1
                    else 0
                }
            }
            else EntityOrder(definition, cl) { e1, e2 ->
                val c1 = e1.getComponent(cl)
                val c2 = e2.getComponent(cl)
                if (c1 == null) {
                    if (c2 == null) 0
                    else 1
                } else {
                    if (c2 == null) -1
                    else 0
                }
            }
        }

    private fun <C> generateEntityOrderBy(definition: EntityOrder.Definition.By<C>): EntityOrder where C : Component, C : Comparable<C> =
        with(definition) {
            if (ascending) {
                if (nullsFirst) EntityOrder(definition, cl) { e1, e2 ->
                    val c1 = e1.getComponent(cl)
                    val c2 = e2.getComponent(cl)
                    if (c1 != null) {
                        if (c2 == null) 1
                        else c1.compareTo(c2)
                    }
                    else if (c2 != null) -1
                    else 0
                }
                else EntityOrder(definition, cl) { e1, e2 ->
                    val c1 = e1.getComponent(cl)
                    val c2 = e2.getComponent(cl)
                    if (c1 != null) {
                        if (c2 == null) -1
                        else c1.compareTo(c2)
                    }
                    else if (c2 != null) 1
                    else 0
                }
            }
            else {
                if (nullsFirst) EntityOrder(definition, cl) { e1, e2 ->
                    val c1 = e1.getComponent(cl)
                    val c2 = e2.getComponent(cl)
                    if (c1 != null) {
                        if (c2 == null) 1
                        else -c1.compareTo(c2)
                    }
                    else if (c2 != null) -1
                    else 0
                }
                else EntityOrder(definition, cl) { e1, e2 ->
                    val c1 = e1.getComponent(cl)
                    val c2 = e2.getComponent(cl)
                    if (c1 != null) {
                        if (c2 == null) -1
                        else -c1.compareTo(c2)
                    }
                    else if (c2 != null) 1
                    else 0
                }
            }
        }

}