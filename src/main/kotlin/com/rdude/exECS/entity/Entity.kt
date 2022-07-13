package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import com.rdude.exECS.event.Event
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.system.System
import com.rdude.exECS.system.ActingSystem
import kotlin.reflect.KClass

/**
 * Entity is a container for components. In exECS Entity is represented as an [id] value and this class is just a wrapper.
 *
 * Entity [id] is guaranteed to remain constant only during the execution of the [World.act] method and may change after
 * the execution is completed. Thus, Entity **MUST NOT BE STORED**, but it can be safely passed along with an [Event]
 * if the [Event] is queued during the execution of the [World.act] method (e.g. inside [ActingSystem.act] method).
 *
 * If compiler plugin is used, explicit calls to [Entity] methods from [Systems][System] and [SingletonEntities][SingletonEntity]
 * will be optimized at compile time by the plugin. Implicit calls will remain as is.
 *
 * @see SingletonEntity
 * @see EntityMethods*/
@JvmInline
value class Entity @PublishedApi internal constructor(val id: Int) {

    /** Get [Component] of type [T] or null if this Entity does not have component of such type.*/
    internal fun <T : Component> getComponent(componentClass: KClass<T>, world: World): T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][id] as T?


    /** Get [Component] of type [T] or null if this Entity does not have component of such type.*/
    internal inline fun <reified T : Component> getComponent(fromWorld: World): T? = getComponent(T::class, fromWorld)


    /** Removes [Component] of the specified type from this Entity. Fires [ComponentRemovedEvent].*/
    internal fun removeComponent(componentClass: KClass<out Component>, world: World) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][id] = null
    }


    /** Removes [Component] of the specified type from this Entity. Fires [ComponentRemovedEvent].*/
    internal inline fun <reified T : Component> removeComponent(fromWorld: World) = removeComponent(T::class, fromWorld)


    /** Returns true if this Entity has a [Component] of the specified type.*/
    internal fun hasComponent(componentClass: KClass<out Component>, world: World): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(id)


    /** Returns true if this Entity has a [Component] of the specified type.*/
    internal inline fun <reified T : Component> hasComponent(world: World) = hasComponent(T::class, world)


    /** Adds [component] to this Entity. Fires [ComponentAddedEvent].*/
    internal fun addComponent(component: Component, world: World) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(id, component)


    /** Obtains [Component] of type [T] from the default Pool and adds it to this Entity. Fires [ComponentAddedEvent].*/
    internal inline fun <reified T> addComponent(world: World): T where T : Component, T : Poolable {
        val component = ExEcs.defaultPools[T::class].obtain() as T
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(id, component)
        return component
    }


    /** Obtains [Component] of type [T] from the default Pool, apply [apply] function to this [Component] and adds it
     *  to this Entity. Fires [ComponentAddedEvent].*/
    internal inline fun <reified T> addComponent(world: World, apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = ExEcs.defaultPools[T::class].obtain() as T
        apply.invoke(component)
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(id, component)
        return component
    }


    /** Removes this Entity from the [World]. Fires [EntityRemovedEvent].*/
    internal fun remove(fromWorld: World) = fromWorld.requestRemoveEntity(id)

}