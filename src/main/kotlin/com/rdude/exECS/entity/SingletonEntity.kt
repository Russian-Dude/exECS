package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.exception.DefaultPoolNotExistException
import com.rdude.exECS.exception.WorldNotSetException
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import kotlin.reflect.KClass

/** SingletonEntity is an Entity that keeps its id constant, so it can be referenced safely, it caches plugged
 * [Components][Component] so they can be accessed independently of the [World], and it can have its own logic and/or
 * extra properties (prefer to keep any logic in [Systems][System] and data in [Components][Component] anyway).
 * [Worlds][World] can have only one instance of each Singleton Entity subtype, and every instance of Singleton Entity
 * can only be plugged into one [World] instance.*/
abstract class SingletonEntity : WorldAccessor() {

    @Transient
    @JvmField
    val entityID: Int = ExEcs.singletonEntityIDsResolver.idFor(this::class)

    @Transient
    override var world: World? = null
        internal set

    /** Provides access to Components independently of the World.*/
    @Transient
    @JvmField
    internal val componentsCache: Array<Component?> = Array(ExEcs.componentTypeIDsResolver.size) { null }


    /** @return this as [Entity].*/
    fun asEntity(): Entity = Entity(entityID)


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.*/
    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(componentClass: KClass<T>): T? =
        componentsCache[componentClass.componentTypeId] as T?


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.*/
    inline fun <reified T : Component> getComponent(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.*/
    inline operator fun <reified T : Component> invoke(): T? = getComponent(T::class)


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.*/
    operator fun <T : Component> get(componentClass: KClass<T>): T? = getComponent(componentClass)


    /** @return [Component] of type [T] or null if this SingletonEntity does not have component of such type.
     * @throws [ArrayIndexOutOfBoundsException]
     * @throws [ClassCastException]*/
    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T : Component> getComponent(componentTypeId: Int): T? = componentsCache[componentTypeId] as T?


    /** Removes [Component] of type [T] from this SingletonEntity.
     * Queues [ComponentRemovedEvent] in the [World] in which this SingletonEntity is registered, if Component has been removed.*/
    fun <T : Component> removeComponent(componentClass: KClass<T>) {
        if (world != null) {
            world!!.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].removeComponent(entityID)
        }
        else componentsCache[componentClass.componentTypeId] = null
    }


    /** Removes [Component] of type [T] from this SingletonEntity.
     * Queues [ComponentRemovedEvent] in the [World] in which this SingletonEntity is registered, if Component has been removed.*/
    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)


    /** Removes [Component] with [componentTypeId] from this SingletonEntity.
     * Queues [ComponentRemovedEvent] in the [World] in which this SingletonEntity is registered, if Component has been removed.*/
    @PublishedApi
    internal fun removeComponent(componentTypeId: Int) {
        if (world != null) {
            world!!.entityMapper.componentMappers[componentTypeId].removeComponent(entityID)
        }
        else componentsCache[componentTypeId] = null
    }


    /** Removes [Component] of type [T] from this SingletonEntity.
     * Queues [ComponentRemovedEvent] in the [World] in which this SingletonEntity is registered, if Component has been removed.*/
    operator fun <T : Component> minusAssign(componentClass: KClass<T>) = removeComponent(componentClass)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this SingletonEntity has a [Component] of type [T].*/
    fun <T : Component> hasComponent(componentClass: KClass<T>): Boolean =
        componentsCache[componentClass.componentTypeId] != null


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this SingletonEntity has a [Component] of type [T].*/
    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)


    /** Same as [getComponent]<[T]>() != null.
     * @return True if this SingletonEntity has a [Component] of type [T].*/
    operator fun <T : Component> contains(componentClass: KClass<T>) = hasComponent(componentClass)


    /** @return True if this SingletonEntity has a [Component] with [componentTypeId].
     * @throws [ArrayIndexOutOfBoundsException]
     * @throws [ClassCastException]*/
    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun hasComponent(componentTypeId: Int): Boolean = componentsCache[componentTypeId] != null


    /** Adds [component] to this SingletonEntity.
     * Queues [ComponentAddedEvent] in the [World] in which this SingletonEntity is registered, if Component has been added.*/
    fun addComponent(component: Component) {
        if (world != null) {
            world!!.entityMapper.componentMappers[component.getComponentTypeId()].addComponentUnsafe(entityID, component)
        }
        else componentsCache[component.getComponentTypeId()] = component
    }


    /** Adds [component] to this SingletonEntity.
     * Queues [ComponentAddedEvent] in the [World] in which this SingletonEntity is registered, if Component has been added.*/
    operator fun plusAssign(component: Component) = addComponent(component)


    /** Obtains a [Component] of type [T] from the default Pool and adds it to this SingletonEntity.
     * Queues [ComponentAddedEvent] in the [World] in which this SingletonEntity is registered, if Component has been added.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist.*/
    inline fun <reified T> addComponent() where T : Component, T : Poolable  =
        addComponent(fromPool<T>())


    /** Obtains [Component] of type [T] from the default Pool, apply [apply] function to this [Component] and adds it
     * to this SingletonEntity.
     * Queues [ComponentAddedEvent] in the [World] in which this SingletonEntity is registered, if Component has been added.
     * @throws [DefaultPoolNotExistException] if default Pool for type [T] does not exist*/
    inline fun <reified T> addComponent(apply: T.() -> Unit) where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
    }


    /** Removes this SingletonEntity from the [World]. Queues [EntityRemovedEvent].
     * @throws [WorldNotSetException] if this SingletonEntity is not registered in the [World]*/
    fun remove() = (world ?: throw WorldNotSetException(this)).requestRemoveEntity(entityID)


    /** Generates a List of [Components][Component] plugged into this SingletonEntity.*/
    fun generateComponentsList(): List<Component> = componentsCache.filterNotNull()

}