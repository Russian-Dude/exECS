package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class EntityWrapper(val world: World) {

    internal var entity: Entity = Entity.DUMMY_ENTITY
    internal var entityID: EntityID = EntityID.DUMMY_ENTITY_ID


    fun <T : Component> getComponent(componentClass: KClass<T>) : T? = entity.getComponent(componentClass)

    fun removeComponent(componentClass: KClass<out Component>) {
        val removedComponent = entity.removeComponent(componentClass)
        if (removedComponent != null) {
            val event = world.componentRemovedEventPool.obtain()
            event.component = removedComponent
            event.pureEntity = entity
            event.entityId = entityID
            world.queueEvent(event)
        }
    }

    fun hasComponent(componentClass: KClass<out Component>) = entity.hasComponent(componentClass)

    fun hasComponents(vararg components: KClass<out Component>) = entity.hasComponents(*components)

    fun addComponent(component: Component) {
        entity.addComponent(component)
        val event = world.componentAddedEventPool.obtain()
        event.component = component
        event.pureEntity = entity
        event.entityId = entityID
        world.queueEvent(event)
    }

    fun remove()  {
        world.removeEntity(entityID)
    }

    inline fun <reified T : Component> getComponent() : T? = getComponent(T::class)

    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)

    inline fun <reified T : Component> hasComponent() = hasComponents(T::class)

    operator fun plusAssign(component: Component) = addComponent(component)

    operator fun minusAssign(componentClass: KClass<out Component>) = removeComponent(componentClass)

    operator fun contains(componentClass: KClass<out Component>) = entity.hasComponent(componentClass)

    operator fun <T : Component> get(componentClass: KClass<T>) : T = entity[componentClass]

}