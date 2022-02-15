package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentRetriever
import com.rdude.exECS.component.ComponentTypeID
import com.rdude.exECS.component.ComponentTypeIDsResolver
import kotlin.reflect.KClass

@JvmInline
internal value class Entity private constructor(private val components: Array<Component?> = Array(ComponentTypeIDsResolver.maxIndex) { null }) {

    @Suppress("UNCHECKED_CAST")
    inline fun <T : Component> getComponent(componentClass: KClass<T>) : T? = components[ComponentTypeIDsResolver.idFor(componentClass).id] as T?

    inline fun <T : Component> getComponent(componentRetriever: ComponentRetriever<T>) : T? = components[componentRetriever.componentTypeID.id] as T?

    inline fun removeComponent(componentClass: KClass<out Component>) : Component? {
        val prevComponent = components[ComponentTypeIDsResolver.idFor(componentClass).id]
        components[ComponentTypeIDsResolver.idFor(componentClass).id] = null
        return prevComponent
    }

    inline fun hasComponent(componentClass: KClass<out Component>) = getComponent(componentClass) != null

    inline fun hasComponents(components: Array<out KClass<out Component>>): Boolean {
        for (i in 0..components.size - 1) {
            if (!hasComponent(components[i])) {
                return false
            }
        }
        return true
    }

    inline fun addComponent(component: Component)  {
        components[ComponentTypeIDsResolver.idFor(component::class).id] = component
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Component> getComponent() : T? = getComponent(T::class)

    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)

    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)

    inline operator fun plusAssign(component: Component) {
        addComponent(component)
    }

    inline operator fun minusAssign(componentClass: KClass<out Component>)  {
        removeComponent(componentClass)
    }

    inline operator fun contains(componentClass: KClass<out Component>) = hasComponent(componentClass)

    inline operator fun <T : Component> get(componentClass: KClass<T>) : T = getComponent(componentClass) as T

    inline operator fun <T : Component> get(componentRetriever: ComponentRetriever<T>) : T =
        components[componentRetriever.componentTypeID.id] as T


    companion object {

        val DUMMY_ENTITY = Entity()

        internal fun new(components: Array<out Component>): Entity {
            val entity = Entity()
            for (i in 0..components.size - 1) {
                entity.addComponent(components[i])
            }
            return entity
        }

    }

}

