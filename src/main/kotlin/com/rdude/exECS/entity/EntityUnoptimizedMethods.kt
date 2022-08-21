package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.exception.DefaultPoolNotExistException
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import kotlin.reflect.KClass

/** [Entity] methods that can be called outside of [WorldAccessor] context.
 * Calls to these methods will not be optimized by the exECS compiler plugin.*/
object EntityUnoptimizedMethods {

    /** Get [Component] of type [T] or null if [entity] does not have component of such type.*/
    fun <T : Component> getComponent(entity: Entity, componentClass: KClass<T>, world: World) : T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entity.id] as T?


    /** Get [Component] of type [T] or null if [entity] does not have component of such type.*/
    inline fun <reified T : Component> getComponent(entity: Entity, fromWorld: World) : T? =
        getComponent(entity, T::class, fromWorld)


    /** Removes [Component] of the specified type from [entity]. Fires [ComponentRemovedEvent].*/
    fun removeComponent(entity: Entity, componentClass: KClass<out Component>, world: World) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entity.id] = null
    }


    /** Removes [Component] of the specified type from [entity]. Fires [ComponentRemovedEvent].*/
    inline fun <reified T : Component> removeComponent(entity: Entity, fromWorld: World) =
        removeComponent(entity, T::class, fromWorld)


    /** Returns true if [entity] has a [Component] of the specified type.*/
    fun hasComponent(entity: Entity, componentClass: KClass<out Component>, world: World): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(entity.id)


    /** Returns true if [entity] has a [Component] of the type [T].*/
    inline fun <reified T : Component> hasComponent(entity: Entity, world: World) =
        hasComponent(entity, T::class, world)


    /** Adds [component] to the [entity]. Fires [ComponentAddedEvent].*/
    fun addComponent(entity: Entity, component: Component, world: World) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entity.id, component)


    /** Obtains [Component] of type [T] from the default Pool and adds it to the [entity]. Fires [ComponentAddedEvent].*/
    fun <T> addComponent(entity: Entity, type: KClass<T>, world: World) where T : Component, T : Poolable {
        val component = ExEcs.defaultPools[type].obtain() as T
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entity.id, component)
    }


    /** Obtains [Component] of type [T] from the default Pool and adds it to the [entity]. Fires [ComponentAddedEvent].*/
    inline fun <reified T> addComponent(entity: Entity, world: World) where T : Component, T : Poolable =
        addComponent(entity, T::class, world)


    /** Obtains [Component] of type [T] from the default Pool, apply [apply] function to this [Component] and adds it
     *  to the [entity]. Fires [ComponentAddedEvent].*/
    inline fun <reified T> addComponent(entity: Entity, world: World, apply: T.() -> Unit) where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        return addComponent(entity, component::class, world)
    }


    /** Removes the [entity] from the [World]. Fires [EntityRemovedEvent].*/
    fun remove(entity: Entity, fromWorld: World) = fromWorld.requestRemoveEntity(entity.id)


    /** Generates a List with [Components][Component] plugged into [entity].*/
    internal fun generateComponentsList(entity: Entity, world: World) =
        world.entityMapper.componentMappers.mapNotNull { it[entity.id] }

}