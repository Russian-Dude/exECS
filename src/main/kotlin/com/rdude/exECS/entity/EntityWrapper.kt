package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.utils.Dummies
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

/**
 * Wraps entity ID.
 * Do not store reference to entity wrapper because wrapped ID is changed consistently.
 * If compiler plugin is used, explicit calls to methods will be optimized at compile time by the plugin.
 * Implicit calls will remain as is.
 */
class EntityWrapper(val world: World) {

    var entityID: Int = Dummies.DUMMY_ENTITY_ID
        internal set


    fun <T : Component> getComponent(componentClass: KClass<T>) : T? =
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)][entityID] as T?

    fun removeComponent(componentClass: KClass<out Component>) {
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }

    fun hasComponent(componentClass: KClass<out Component>): Boolean =
        world.entityMapper.componentMappers[ComponentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)

    fun addComponent(component: Component) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)

    fun remove() = world.removeEntity(entityID)

    inline fun <reified T : Component> getComponent() : T? = getComponent(T::class)

    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)

    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)

    operator fun plusAssign(component: Component) = addComponent(component)

    operator fun minusAssign(componentClass: KClass<out Component>) = removeComponent(componentClass)

    operator fun contains(componentClass: KClass<out Component>) = hasComponent(componentClass)

    operator fun <T : Component> get(componentClass: KClass<T>) =
        getComponent(componentClass) ?: throw IllegalStateException("Entity does not have a component of type $componentClass.")

}