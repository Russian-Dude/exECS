package com.rdude.exECS.component

import com.rdude.exECS.component.state.ComponentStateManager
import com.rdude.exECS.component.state.componentStateManagerForType
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ComponentTypeToEntityPair
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class ComponentMapper<T : Component> private constructor(
    private val world: World,
    private val stateManager: ComponentStateManager<*>?,
    @JvmField internal var backingArray: Array<T?>,
    @JvmField internal val componentTypeId: Int,
    @JvmField internal var sendComponentAddedEvents: Boolean = false,
    @JvmField internal var sendComponentRemovedEvents: Boolean = false
) {

    operator fun get(id: Int) = backingArray[id]

    operator fun set(id: Int, component: T?) {
        if (component == null) removeComponent(id)
        else addComponent(id, component)
    }

    fun hasComponent(id: Int): Boolean {
        return backingArray[id] != null
    }

    fun removeComponent(id: Int) {
        val removedComponent = backingArray[id] ?: return
        backingArray[id] = null
        // update component states if needed
        stateManager?.componentRemovedUnsafe(removedComponent, id)
        // notify subscribersManager
        world.componentPresenceChange(ComponentTypeToEntityPair(entityID = id, componentTypeId = componentTypeId))
        // queue event
        if (sendComponentRemovedEvents) {
            val event = ComponentRemovedEvent.pool.obtain()
            event.component = removedComponent
            event.entity = Entity(id)
            world.queueEvent(event)
        }
        // notify about internal change
        world.internalChangeOccurred = true
    }

    fun addComponent(id: Int, component: T): T {
        val removedComponent = backingArray[id]
        // if adding same component just return it
        if (removedComponent == component) return removedComponent
        // if component was replaced update removed component states if needed
        if (removedComponent != null) {
            stateManager?.componentRemovedUnsafe(removedComponent, id)
        }
        // add component to the actual entity
        backingArray[id] = component
        // update component states if needed
        stateManager?.componentAddedUnsafe(component, id)
        // notify subscribersManager
        world.componentPresenceChange(ComponentTypeToEntityPair(entityID = id, componentTypeId = componentTypeId))
        // queue events
        if (sendComponentAddedEvents) {
            val event = ComponentAddedEvent.pool.obtain()
            event.component = component
            event.entity = Entity(id)
            event.replacedComponent = removedComponent
            world.queueEvent(event)
        }
        // notify about internal change
        world.internalChangeOccurred = true
        return component
    }

    inline fun addComponent(id: Int, component: T, apply: T.() -> Unit): T {
        apply.invoke(component)
        addComponent(id, component)
        return component
    }

    internal fun removeComponentSilently(id: Int, sendComponentPresenceChange: Boolean = false) {
        val removedComponent = backingArray[id] ?: return
        backingArray[id] = null
        if (sendComponentPresenceChange) {
            world.componentPresenceChange(ComponentTypeToEntityPair(entityID = id, componentTypeId = componentTypeId))
        }
        stateManager?.componentRemovedUnsafe(removedComponent, id)
    }

    internal fun replaceId(fromId: Int, toId: Int) {
        val componentFrom = backingArray[fromId]
        val componentTo = backingArray[toId]
        backingArray[toId] = componentFrom
        backingArray[fromId] = componentTo
        if (componentFrom != null) {
            stateManager?.componentChangedIdUnsafe(componentFrom, fromId, toId)
        }
        if (componentTo != null) {
            stateManager?.componentChangedIdUnsafe(componentTo, toId, fromId)
        }
    }

    internal fun clear() {
        for (i in backingArray.indices) {
            removeComponentSilently(i)
        }
        world.internalChangeOccurred = true
    }

    internal fun unsafeSet(id: Int, component: Any?) = set(id, component as T?)

    internal fun grow(newSize: Int) {
        backingArray = backingArray.copyOf(newSize)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun setBackingArrayUnsafe(array: Array<*>) {
        backingArray = array as Array<T?>
    }


    internal companion object {
        operator fun <T : Component> invoke(type: KClass<T>, world: World, initialSize: Int) =
            ComponentMapper(
                world = world,
                stateManager = componentStateManagerForType(type, world),
                backingArray = java.lang.reflect.Array.newInstance(type.java, initialSize) as Array<T?>,
                componentTypeId = type.componentTypeId
            )

        operator fun invoke(componentId: Int, world: World, initialSize: Int) =
            invoke(ExEcs.componentTypeIDsResolver.typeById(componentId), world, initialSize)
    }

}