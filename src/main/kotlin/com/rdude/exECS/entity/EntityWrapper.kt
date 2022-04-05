package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

/**
 * Wraps entity ID.
 * Do not store reference to entity wrapper because wrapped ID is changed consistently.
 * If compiler plugin is used, explicit calls to methods will be optimized at compile time by the plugin.
 * Implicit calls will remain as is.
 */
@JvmInline
value class EntityWrapper internal constructor(val entityID: Int) {

    internal fun <T : Component> getComponent(componentClass: KClass<T>, world: World) : T? =
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)][entityID] as T?

    internal fun removeComponent(componentClass: KClass<out Component>, world: World) {
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }

    internal fun hasComponent(componentClass: KClass<out Component>, world: World): Boolean =
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)

    internal fun addComponent(component: Component, world: World) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)

    internal inline fun <reified T> addComponent(): T where T : Component, T : Poolable {
        TODO ("create this method and add it to compiler plugin")
    }

    internal inline fun <reified T> addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        TODO ("create this method and add it to compiler plugin")
    }

    internal fun remove(fromWorld: World) = fromWorld.removeEntity(entityID)

    internal inline fun <reified T : Component> getComponent(fromWorld: World) : T? = getComponent(T::class, fromWorld)

    internal inline fun <reified T : Component> removeComponent(fromWorld: World) = removeComponent(T::class, fromWorld)

    internal inline fun <reified T : Component> hasComponent(world: World) = hasComponent(T::class, world)

}