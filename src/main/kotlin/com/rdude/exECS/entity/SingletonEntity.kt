package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

abstract class SingletonEntity {

    init {
        ExEcs.initializeIfNeeded()
    }

    @Transient
    val entityID: Int = ExEcs.singletonEntityIDsResolver.getId(this::class)

    @Transient
    lateinit var world: World
        internal set

    internal val isWorldInitialized get() = ::world.isInitialized


    fun <T : Component> getComponent(componentClass: KClass<T>): T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] as T?

    fun removeComponent(componentClass: KClass<out Component>) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }

    fun hasComponent(componentClass: KClass<out Component>): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)

    fun addComponent(component: Component) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)

    inline fun <reified T> addComponent(): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        addComponent(component)
        return component
    }

    inline fun <reified T> addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
        return component
    }

    fun remove() = world.removeEntity(entityID)

    inline fun <reified T : Component> getComponent(): T? = getComponent(T::class)

    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)

    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)

    operator fun plusAssign(component: Component) = addComponent(component)

    operator fun minusAssign(componentClass: KClass<out Component>) =
        removeComponent(componentClass)

    operator fun contains(componentClass: KClass<out Component>) = hasComponent(componentClass)

    operator fun <T : Component> get(componentClass: KClass<T>) =
        getComponent(componentClass)
            ?: throw IllegalStateException("Entity does not have a component of type $componentClass.")

}