package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class Entity private constructor(private val components: MutableMap<KClass<out Component>, Component> = HashMap()) {

    private lateinit var world: World

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(componentClass: KClass<T>) : T? = components[componentClass] as T?

    fun removeComponent(componentClass: KClass<out Component>)  {
        val component = components.remove(componentClass)
        if (component != null) {
            val event = componentRemovedEventPool.obtain()
            event.component = component
            event.entity = this
            world.queueEvent(event)
        }
    }

    fun hasComponent(componentClass: KClass<out Component>) = components.containsKey(componentClass)

    fun hasComponents(vararg components: KClass<out Component>): Boolean {
        for (i in 0..components.size) {
            if (!hasComponent(components[i])) {
                return false
            }
        }
        return true
    }

    fun addComponent(component: Component)  {
        components[component::class] = component
        val event = componentAddedEventPool.obtain()
        event.component = component
        event.entity = this
        world.queueEvent(event)
    }

    private fun addComponentSilently(component: Component) = components.put(component::class, component)

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

        private val componentAddedEventPool: Pool<ComponentAddedEvent> by lazy { Pool { ComponentAddedEvent(Component.DUMMY_COMPONENT, DUMMY_ENTITY) } }
        private val componentRemovedEventPool: Pool<ComponentRemovedEvent> by lazy { Pool { ComponentRemovedEvent(Component.DUMMY_COMPONENT, DUMMY_ENTITY) } }

        val DUMMY_ENTITY = Entity()

        fun new(world: World, vararg components: Component): Entity {
            val entity = Entity()
            entity.world = world
            for (i in 0..components.size) {
                entity.addComponentSilently(components[i])
            }
            return entity
        }

    }

}

