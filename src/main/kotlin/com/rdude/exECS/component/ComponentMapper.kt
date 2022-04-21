package com.rdude.exECS.component

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.BitSet
import com.rdude.exECS.utils.collections.ResizableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class ComponentMapper<T : Component> private constructor(
    internal var backingArray: Array<T?>, private val world: World, internal val componentTypeId: Int
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
        val removedComponent = backingArray[id]
        backingArray[id] = null
        if (removedComponent != null) {
            // update component entity amount
            removedComponent.insideEntities--
            if (removedComponent.insideEntities == 0 && removedComponent is Poolable) {
                world.poolablesToReturn.add(removedComponent)
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
            val event = ComponentRemovedEvent.pool.obtain()
            event.component = removedComponent
            event.entity = EntityWrapper(id)
            world.queueInternalEvent(event)
            world.internalChangeOccurred = true
        }
    }

    fun addComponent(id: Int, component: T): T {
        val removedComponent = backingArray[id]
        if (removedComponent != null && removedComponent != component) {
            removedComponent.insideEntities--
            if (removedComponent.insideEntities == 0 && removedComponent is Poolable) {
                world.poolablesToReturn.add(removedComponent)
            }
        }
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
        val event = ComponentAddedEvent.pool.obtain()
        event.component = component
        event.entity = EntityWrapper(id)
        event.replacedComponent = removedComponent
        world.queueInternalEvent(event)
        world.internalChangeOccurred = true
        return component
    }

    inline fun addComponent(id: Int, component: T, apply: T.() -> Unit): T {
        apply.invoke(component)
        addComponent(id, component)
        return component
    }

    internal fun removeComponentSilently(id: Int) {
        val removedComponent = backingArray[id]
        backingArray[id] = null
        if (removedComponent != null) {
            // update component entity amount
            removedComponent.insideEntities--
            if (removedComponent.insideEntities == 0 && removedComponent is Poolable) {
                world.poolablesToReturn.add(removedComponent)
            }
        }
    }

    internal fun replaceId(fromId: Int, toId: Int) {
        backingArray[toId] = backingArray[fromId]
        backingArray[fromId] = null
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

    internal fun setBackingArrayUnsafe(newArray: Array<*>) {
        backingArray = newArray as Array<T?>
    }


    internal companion object {
        operator fun <T : Component> invoke(type: KClass<T>, world: World, initialSize: Int, componentTypeId: Int) =
            ComponentMapper(
                java.lang.reflect.Array.newInstance(type.java, initialSize) as Array<T?>,
                world,
                componentTypeId
            )

        operator fun invoke(componentId: Int, world: World, initialSize: Int) =
            invoke(ExEcs.componentTypeIDsResolver.typeById(componentId), world, initialSize, componentId)
    }

}