package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentPresenceChange
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class EntityWrapper(val world: World) {

    internal var entity: Entity = Entity.DUMMY_ENTITY
    internal var entityID: EntityID = EntityID.DUMMY_ENTITY_ID
    private var removed = false


    fun <T : Component> getComponent(componentClass: KClass<T>) : T? = entity.getComponent(componentClass)

    fun removeComponent(componentClass: KClass<out Component>) {
        val removedComponent = entity.removeComponent(componentClass)
        if (removedComponent != null) {
            // notify subscribersManager
            world.componentPresenceChange(ComponentPresenceChange(
                entityID = entityID,
                componentId = ComponentTypeIDsResolver.idFor(componentClass),
                removed = true))
            // queue event
            val event = world.componentRemovedEventPool.obtain()
            event.component = removedComponent
            event.pureEntity = entity
            event.entityId = entityID
            world.queueInternalEvent(event)
        }
    }

    fun hasComponent(componentClass: KClass<out Component>) = entity.hasComponent(componentClass)

    fun hasComponents(vararg components: KClass<out Component>) = entity.hasComponents(components)

    fun addComponent(component: Component) {
        // add component to the actual entity
        entity.addComponent(component)
        // notify subscribersManager
        world.componentPresenceChange(ComponentPresenceChange(
            entityID = entityID,
            componentId = ComponentTypeIDsResolver.idFor(component::class),
            removed = false))
        // queue event
        val event = world.componentAddedEventPool.obtain()
        event.component = component
        event.pureEntity = entity
        event.entityId = entityID
        world.queueInternalEvent(event)
    }

    fun remove()  {
        world.removeEntity(entityID)
    }

    inline fun <reified T : Component> getComponent() : T? = getComponent(T::class)

    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)

    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)

    operator fun plusAssign(component: Component) = addComponent(component)

    operator fun minusAssign(componentClass: KClass<out Component>) = removeComponent(componentClass)

    operator fun contains(componentClass: KClass<out Component>) = entity.hasComponent(componentClass)

    operator fun <T : Component> get(componentClass: KClass<T>) : T = entity[componentClass]

}