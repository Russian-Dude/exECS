package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

/**
 * Wraps an entity ID.
 * Do not store reference to entity wrapper because wrapped ID is changed consistently.
 * Also prefer not to pass entity wrapper between worlds because entity wrapper is just an int value and represents
 * different entities in different worlds.
 * All methods in this class declared as internal because preferred way to interact with entity wrapper is by
 * using equivalent methods in [System] and [SingletonEntity] classes.
 * Those are the same methods but without need to pass world instance as an argument.
 * However, if there is still a need to use entity wrapper outside of systems and singletons, use [EntityWrapperMethods] object.
 * If compiler plugin is used, explicit calls to methods (in systems and singleton entities)
 * will be optimized at compile time by the plugin.
 * Implicit calls will remain as is.
 */
@JvmInline
value class EntityWrapper internal constructor(val entityID: Int) {

    internal fun <T : Component> getComponent(componentClass: KClass<T>, world: World) : T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] as T?

    internal fun removeComponent(componentClass: KClass<out Component>, world: World) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }

    internal fun hasComponent(componentClass: KClass<out Component>, world: World): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)

    internal fun addComponent(component: Component, world: World) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)

    internal inline fun <reified T> addComponent(world: World): T where T : Component, T : Poolable {
        val component = ExEcs.defaultPools[T::class].obtain() as T
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)
        return component
    }

    internal inline fun <reified T> addComponent(world: World, apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = ExEcs.defaultPools[T::class].obtain() as T
        apply.invoke(component)
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)
        return component
    }

    internal fun remove(fromWorld: World) = fromWorld.removeEntity(entityID)

    internal inline fun <reified T : Component> getComponent(fromWorld: World) : T? = getComponent(T::class, fromWorld)

    internal inline fun <reified T : Component> removeComponent(fromWorld: World) = removeComponent(T::class, fromWorld)

    internal inline fun <reified T : Component> hasComponent(world: World) = hasComponent(T::class, world)

}