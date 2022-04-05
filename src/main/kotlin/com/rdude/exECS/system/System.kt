package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.Event
import com.rdude.exECS.inject.SystemDelegate
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.reflection.GeneratedFieldsInitializer
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

abstract class System {

    abstract val aspect: Aspect

    lateinit var world: World
        private set

    internal lateinit var entitiesSubscription: EntitiesSubscription

    fun createEntity(vararg components: Component) = world.createEntity(*components)

    fun queueEvent(event: Event) = world.queueEvent(event)

    internal fun setWorld(world: World) {
        this.world = world
        GeneratedFieldsInitializer.initialize(this)
    }

    protected inline fun <reified T : System> inject() = SystemDelegate(T::class)

    protected fun <T : Component> EntityWrapper.getComponent(componentClass: KClass<T>): T? =
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)][entityID] as T?

    protected fun EntityWrapper.removeComponent(componentClass: KClass<out Component>) {
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }

    protected fun EntityWrapper.hasComponent(componentClass: KClass<out Component>): Boolean =
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)

    protected fun EntityWrapper.addComponent(component: Component) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)

    internal inline fun <reified T> addComponent(): T where T : Component, T : Poolable {
        TODO ("create this method and add it to compiler plugin")
    }

    internal inline fun <reified T> addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        TODO ("create this method and add it to compiler plugin")
    }

    protected fun EntityWrapper.remove() = world.removeEntity(entityID)

    protected inline fun <reified T : Component> EntityWrapper.getComponent(): T? = getComponent(T::class)

    protected inline fun <reified T : Component> EntityWrapper.removeComponent() = removeComponent(T::class)

    protected inline fun <reified T : Component> EntityWrapper.hasComponent() = hasComponent(T::class)

    protected operator fun EntityWrapper.plusAssign(component: Component) = addComponent(component)

    protected operator fun EntityWrapper.minusAssign(componentClass: KClass<out Component>) =
        removeComponent(componentClass)

    protected operator fun EntityWrapper.contains(componentClass: KClass<out Component>) = hasComponent(componentClass)

    protected operator fun <T : Component> EntityWrapper.get(componentClass: KClass<T>) =
        getComponent(componentClass)
            ?: throw IllegalStateException("Entity does not have a component of type $componentClass.")

    override fun toString(): String {
        return "System-${this::class.simpleName}"
    }


    protected companion object {
        @JvmStatic
        protected infix fun KClass<out Component>.and(other: KClass<out Component>): MutableList<KClass<out Component>> =
            mutableListOf(this, other)

        @JvmStatic
        protected infix fun MutableList<KClass<out Component>>.and(other: KClass<out Component>): MutableList<KClass<out Component>> {
            add(other)
            return this
        }
    }
}