package com.rdude.exECS.component

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class ComponentMapper<T : Component> private constructor(
    private var backingArray: Array<T?>, private val world: World, private val componentTypeId: Int
) {

    // Components should be removed to pool only after componentRemoved event with this component is fired
    internal val componentsToReturnToPool = IterableArray<Poolable>()

    operator fun get(id: Int) = backingArray[id]

    operator fun set(id: Int, component: T?) {
        if (component == null) removeComponent(id)
        else addComponent(id, component)
    }

    fun hasComponent(id: Int): Boolean {
        return backingArray[id] != null
    }

    internal fun clear() {
        backingArray.fill(null)
        world.internalChangeOccurred = true
    }

    internal fun unsafeSet(id: Int, component: Any?) = set(id, component as T?)

    internal fun grow(newSize: Int) {
        backingArray = backingArray.copyOf(newSize)
    }

    fun removeComponent(id: Int) {
        val removedComponent = backingArray[id]
        backingArray[id] = null
        if (removedComponent != null) {
            // update component entity amount
            removedComponent.insideEntities--
            // if poolable queue to return to pool
            if (removedComponent is Poolable && removedComponent.insideEntities <= 0) {
                componentsToReturnToPool.add(removedComponent)
            }
            // notify subscribersManager
            world.componentPresenceChange(
                ComponentPresenceChange(
                    entityID = id,
                    componentId = componentTypeId,
                    removed = true
                )
            )
            // queue event
            val event = world.componentRemovedEventPool.obtain()
            event.component = removedComponent
            event.entity = EntityWrapper(id)
            world.queueInternalEvent(event)
            world.internalChangeOccurred = true
        }
    }

    fun addComponent(id: Int, component: T) {
        val removedComponent = backingArray[id]
        if (removedComponent == component) return
        // add component to the actual entity
        backingArray[id] = component
        // update component entity amount
        component.insideEntities++
        // notify subscribersManager
        world.componentPresenceChange(
            ComponentPresenceChange(
                entityID = id,
                componentId = componentTypeId,
                removed = false,
            )
        )
        // queue events
        val event = world.componentAddedEventPool.obtain()
        event.component = component
        event.entity = EntityWrapper(id)
        event.replacedComponent = removedComponent
        world.queueInternalEvent(event)
        world.internalChangeOccurred = true
    }

    internal companion object {
        operator fun <T : Component> invoke(type: KClass<T>, world: World, initialSize: Int, componentTypeId: Int) =
            ComponentMapper(
                java.lang.reflect.Array.newInstance(type.java, initialSize) as Array<T?>,
                world,
                componentTypeId
            )

        operator fun invoke(componentId: Int, world: World, initialSize: Int) =
            invoke(ComponentTypeIDsResolver.typeById(componentId), world, initialSize, componentId)
    }

}