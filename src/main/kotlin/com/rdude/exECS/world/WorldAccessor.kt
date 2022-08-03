package com.rdude.exECS.world

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.exception.AlreadyRegisteredException
import com.rdude.exECS.exception.DefaultPoolNotExistException
import com.rdude.exECS.exception.EmptyEntityException
import com.rdude.exECS.exception.WorldNotSetException
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

/** Provides access to the [World] and [Entity] methods directly from the inheritors of this class.
 * Calls to the methods declared in this class will be optimized by the exECS [compiler plugin](https://github.com/Russian-Dude/execs-plugin).*/
// As soon as a version of Kotlin is released that supports both compiler plugins and context this class will be
// refactored - methods will be moved to external functions with context and this class will become an interface.
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
     protected fun queueEvent(event: Event) =
        world?.queueEvent(event) ?: throw WorldNotSetException(this)


    /** Obtains an [Event] of type [T] from the default Pool and queues it.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T> queueEvent() where T : Event, T : Poolable =
        world?.queueEvent<T>() ?: throw WorldNotSetException(this)


    /** Obtains an [Event] of type [T] from the default Pool, applies [apply] function to it and queues this Event.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property is null*/
    protected inline fun <reified T> queueEvent(apply: T.() -> Unit) where T : Event, T : Poolable =
        world?.queueEvent(apply) ?: throw WorldNotSetException(this)


    /** @return [SingletonEntity] of type [T] or null if Singleton with this type is not registered in the [world].
     * @throws [WorldNotSetException] if [world] property is null*/
    @Suppress("UNCHECKED_CAST")
    protected fun <T : SingletonEntity> getSingletonEntity(cl: KClass<T>): T? =
        (world ?: throw WorldNotSetException(this))
            .entityMapper.singletons[ExEcs.singletonEntityIDsResolver.idFor(cl)] as T?


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
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    @Suppress("UNCHECKED_CAST")
    protected fun <T : Component> Entity.getComponent(componentClass: KClass<T>): T? =
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][id] as T?


    /** @return [Component] of type [T] or null if this Entity does not have component of such type.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T : Component> Entity.getComponent(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this Entity does not have component of such type.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline operator fun <reified T : Component> Entity.invoke(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this Entity does not have component of such type.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun <T : Component> Entity.get(componentClass: KClass<T>): T? = getComponent(componentClass)


    /** Removes [Component] of type [T] from this Entity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun <T : Component> Entity.removeComponent(componentClass: KClass<T>) {
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][id] = null
    }


    /** Removes [Component] of type [T] from this Entity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T : Component> Entity.removeComponent() = removeComponent(T::class)


    /** Removes [Component] of type [T] from this Entity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun <T : Component> Entity.minusAssign(componentClass: KClass<T>) = removeComponent(componentClass)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this Entity has a [Component] of type [T].
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun <T : Component> Entity.hasComponent(componentClass: KClass<T>): Boolean =
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(id)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this Entity has a [Component] of type [T].
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T : Component> Entity.hasComponent() = hasComponent(T::class)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this Entity has a [Component] of type [T].
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun <T : Component> Entity.contains(componentClass: KClass<T>) = hasComponent(componentClass)


    /** Adds [component] to this Entity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.addComponent(component: Component) =
        (world ?: throw WorldNotSetException(this@WorldAccessor))
            .entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(id, component)


    /** Adds [component] to this Entity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected operator fun Entity.plusAssign(component: Component) = addComponent(component)


    /** Obtains a [Component] of type [T] from the default Pool and adds it to this Entity.
     * Queues [ComponentAddedEvent]  if Component has been added.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T> Entity.addComponent(): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        addComponent(component)
        return component
    }


    /** Obtains [Component] of type [T] from the default Pool, apply [apply] function to this [Component] and adds it
     * to this Entity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected inline fun <reified T> Entity.addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
        return component
    }


    /** Removes this Entity from the [World]. Queues [EntityRemovedEvent].
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.remove() = (world ?: throw WorldNotSetException(this@WorldAccessor)).requestRemoveEntity(id)


    /** Generates a List of [Components][Component] plugged into this Entity.
     * @throws [WorldNotSetException] if [world] property of the [WorldAccessor] in the context of which this method is called is null*/
    protected fun Entity.generateComponentsList(): List<Component> =
        world?.entityMapper?.componentMappers?.mapNotNull { it[id] } ?: throw WorldNotSetException(this@WorldAccessor)

}