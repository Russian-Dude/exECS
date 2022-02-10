package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import java.util.*
import kotlin.reflect.KClass

@JvmInline
value class Entity private constructor(private val components: MutableMap<KClass<out Component>, Component> = IdentityHashMap()) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(componentClass: KClass<T>) : T? = components[componentClass] as T?

    fun removeComponent(componentClass: KClass<out Component>) : Component? = components.remove(componentClass)

    fun hasComponent(componentClass: KClass<out Component>) = components.containsKey(componentClass)

    fun hasComponents(components: Array<out KClass<out Component>>): Boolean {
        for (i in 0..components.size - 1) {
            if (!hasComponent(components[i])) {
                return false
            }
        }
        return true
    }

    fun addComponent(component: Component)  {
        components[component::class] = component
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Component> getComponent() : T? = getComponent(T::class)

    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)

    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)

    operator fun plusAssign(component: Component) {
        addComponent(component)
    }

    operator fun minusAssign(componentClass: KClass<out Component>)  {
        removeComponent(componentClass)
    }

    operator fun contains(componentClass: KClass<out Component>) = hasComponent(componentClass)

    operator fun <T : Component> get(componentClass: KClass<T>) : T = getComponent(componentClass) as T


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

