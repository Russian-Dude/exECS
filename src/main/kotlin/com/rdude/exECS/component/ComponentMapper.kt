package com.rdude.exECS.component

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.decreaseCount
import com.rdude.exECS.utils.increaseCount
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
            // update component entity amount if it is poolable and request to remove it to the pool if amount is 0
            if (removedComponent is Poolable) {
                val insideEntities =
                    if (removedComponent is PoolableComponent) --removedComponent.insideEntities
                    else PoolableComponent.componentsToInsideEntitiesAmount.decreaseCount(removedComponent)
                if (insideEntities == 0) world.poolablesToReturn.add(removedComponent)
            }
            // if component is rich component, clear it's `inside entity` property
            if (removedComponent is RichComponent) {
                removedComponent.entityId = -1
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
        // if adding same component just return it
        if (removedComponent == component) return removedComponent
        // if component is rich component, set it's `inside entity` property or throw if they already set
        if (component is RichComponent) {
            if (component.entityId >= 0) throw IllegalStateException("Instance of rich component is already plugged into an entity")
            component.entityId = id
        }
        // if component was replaced
        if (removedComponent != null && removedComponent != component) {
            // update removed component entity amount if it is poolable and request to remove it to the pool if amount is 0
            if (removedComponent is Poolable) {
                val insideEntities =
                    if (removedComponent is PoolableComponent) --removedComponent.insideEntities
                    else PoolableComponent.componentsToInsideEntitiesAmount.decreaseCount(removedComponent)
                if (insideEntities == 0) world.poolablesToReturn.add(removedComponent)
            }
            // if removed component is rich component, clear it's `inside entity` property
            if (removedComponent is RichComponent) {
                removedComponent.entityId = -1
            }
        }
        // add component to the actual entity
        backingArray[id] = component
        // update component entity amount if it is poolable
        if (component is Poolable) {
            if (component is PoolableComponent) component.insideEntities++
            else PoolableComponent.componentsToInsideEntitiesAmount.increaseCount(component)
        }
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
            // update component entity amount if it is poolable and request to remove it to the pool if amount is 0
            if (removedComponent is Poolable) {
                val insideEntities =
                    if (removedComponent is PoolableComponent) --removedComponent.insideEntities
                    else PoolableComponent.componentsToInsideEntitiesAmount.decreaseCount(removedComponent)
                if (insideEntities == 0) world.poolablesToReturn.add(removedComponent)
            }
            // if component is rich component, clear it's `inside entity` property
            if (removedComponent is RichComponent) {
                removedComponent.entityId = -1
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