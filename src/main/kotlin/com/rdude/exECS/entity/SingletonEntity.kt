package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.exception.DefaultPoolNotExistException
import com.rdude.exECS.exception.WorldNotSetException
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import kotlin.reflect.KClass

/** [Entity] that keeps its id constant, so it can be referenced safely.
 * Can be accessed anytime by the class name using [World.getSingletonEntity] or [WorldAccessor.getSingletonEntity] methods.
 * Each [World] can only have one instance of each SingletonEntity,
 * and each instance of SingletonEntity can only be plugged into one [World] instance.*/
abstract class SingletonEntity : WorldAccessor() {

    @Transient
    val entityID: Int = ExEcs.singletonEntityIDsResolver.idFor(this::class)

    @Transient
    override var world: World? = null
        internal set


    /** @return this as [Entity].*/
    fun asEntity(): Entity = Entity(entityID)


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(componentClass: KClass<T>): T? =
        (world ?: throw WorldNotSetException(this))
            .entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] as T?


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.
     * @throws @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    inline fun <reified T : Component> getComponent(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    inline operator fun <reified T : Component> invoke(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    operator fun <T : Component> get(componentClass: KClass<T>): T? = getComponent(componentClass)


    /** Removes [Component] of type [T] from this SingletonEntity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    fun <T : Component> removeComponent(componentClass: KClass<T>) {
        (world ?: throw WorldNotSetException(this))
            .entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }


    /** Removes [Component] of type [T] from this SingletonEntity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)


    /** Removes [Component] of type [T] from this SingletonEntity. Queues [ComponentRemovedEvent] if Component has been removed.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    operator fun <T : Component> minusAssign(componentClass: KClass<T>) = removeComponent(componentClass)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this SingletonEntity has a [Component] of type [T].
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    fun <T : Component> hasComponent(componentClass: KClass<T>): Boolean =
        (world ?: throw WorldNotSetException(this))
            .entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this SingletonEntity has a [Component] of type [T].
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this SingletonEntity has a [Component] of type [T].
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    operator fun <T : Component> contains(componentClass: KClass<T>) = hasComponent(componentClass)


    /** Adds [component] to this SingletonEntity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    fun addComponent(component: Component) =
        (world ?: throw WorldNotSetException(this))
            .entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)


    /** Adds [component] to this SingletonEntity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    operator fun plusAssign(component: Component) = addComponent(component)


    /** Obtains a [Component] of type [T] from the default Pool and adds it to this SingletonEntity.
     * Queues [ComponentAddedEvent]  if Component has been added.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    inline fun <reified T> addComponent(): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        addComponent(component)
        return component
    }


    /** Obtains [Component] of type [T] from the default Pool, apply [apply] function to this [Component] and adds it
     * to this SingletonEntity. Queues [ComponentAddedEvent] if Component has been added.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    inline fun <reified T> addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
        return component
    }


    /** Removes this SingletonEntity from the [World]. Queues [EntityRemovedEvent].
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    fun remove() = (world ?: throw WorldNotSetException(this)).requestRemoveEntity(entityID)


    /** Generates a List of [Components][Component] plugged into this SingletonEntity.
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    fun generateComponentsList(): List<Component> =
        world?.entityMapper?.componentMappers?.mapNotNull { it[entityID] } ?: throw WorldNotSetException(this)

}