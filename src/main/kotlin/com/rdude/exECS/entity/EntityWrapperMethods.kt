package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

object EntityWrapperMethods {

    fun <T : Component> getComponent(entityWrapper: EntityWrapper, componentClass: KClass<T>, world: World) : T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityWrapper.entityID] as T?

    fun removeComponent(entityWrapper: EntityWrapper, componentClass: KClass<out Component>, world: World) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityWrapper.entityID] = null
    }

    fun hasComponent(entityWrapper: EntityWrapper, componentClass: KClass<out Component>, world: World): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(entityWrapper.entityID)

    fun addComponent(entityWrapper: EntityWrapper, component: Component, world: World) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityWrapper.entityID, component)

    fun <T> addComponent(entityWrapper: EntityWrapper, type: KClass<T>, world: World): T where T : Component, T : Poolable {
        val component = ExEcs.defaultPools[type].obtain() as T
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityWrapper.entityID, component)
        return component
    }

    inline fun <reified T> addComponent(entityWrapper: EntityWrapper, world: World): T where T : Component, T : Poolable =
        addComponent(entityWrapper, T::class, world)

    inline fun <reified T> addComponent(entityWrapper: EntityWrapper, world: World, apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        return addComponent(entityWrapper, component::class, world)
    }

    fun remove(entityWrapper: EntityWrapper, fromWorld: World) = fromWorld.removeEntity(entityWrapper.entityID)

    inline fun <reified T : Component> getComponent(entityWrapper: EntityWrapper, fromWorld: World) : T? =
        getComponent(entityWrapper, T::class, fromWorld)

    inline fun <reified T : Component> removeComponent(entityWrapper: EntityWrapper, fromWorld: World) =
        removeComponent(entityWrapper, T::class, fromWorld)

    inline fun <reified T : Component> hasComponent(entityWrapper: EntityWrapper, world: World) =
        hasComponent(entityWrapper, T::class, world)

}