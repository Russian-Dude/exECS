package com.rdude.exECS.component

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ComponentTypeToEntityPair
import com.rdude.exECS.utils.decreaseCount
import com.rdude.exECS.utils.increaseCount
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class ComponentMapper<T : Component> private constructor(
    @JvmField internal var backingArray: Array<T?>,
    private val world: World,
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
        val removedComponent = backingArray[id]
        backingArray[id] = null
        if (removedComponent != null) {
            // update component entity amount if it is poolable and request to remove it to the pool if amount is 0
            if (removedComponent is Poolable) {
                val insideEntities =
                    if (removedComponent is PoolableComponent) --removedComponent.insideEntities
                    else PoolableComponent.componentsToInsideEntitiesAmount.decreaseCount(removedComponent)
                if (insideEntities == 0) world.poolablesManager.poolableNeedsToBeReturnedToPool(removedComponent)
            }
            // if component is unique component, clear it's `inside entity` property
            if (removedComponent is UniqueComponent) {
                removedComponent.entityId = -1
            }
            // if component is Rich, remove id from plugged list
            else if (removedComponent is RichComponent) {
                removedComponent.insideEntitiesSet.remove(id)
            }
            // notify subscribersManager
            world.componentPresenceChange(
                ComponentTypeToEntityPair(
                    entityID = id,
                    componentTypeId = componentTypeId,
                )
            )
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
    }

    fun addComponent(id: Int, component: T): T {
        val removedComponent = backingArray[id]
        // if adding same component just return it
        if (removedComponent == component) return removedComponent
        // if component is unique component, set it's `inside entity` property or throw if they already set
        if (component is UniqueComponent) {
            if (component.entityId >= 0) throw IllegalStateException("Instance of unique component is already plugged into an entity")
            component.entityId = id
        }
        // if component is rich component, add id to the `inside entities` set
        else if (component is RichComponent) {
            component.insideEntitiesSet.add(id)
        }
        // if component is observable component set its world
        if (component is ObservableComponent<*>) {
            val world = component.world
            if (world != null && this.world != world) {
                throw IllegalStateException("Observable component is already in a different world")
            }
            if (component.world == null) component.world = this.world
        }
        // if component was replaced
        if (removedComponent != null && removedComponent != component) {
            // update removed component entity amount if it is poolable and request to remove it to the pool if amount is 0
            if (removedComponent is Poolable) {
                val insideEntities =
                    if (removedComponent is PoolableComponent) --removedComponent.insideEntities
                    else PoolableComponent.componentsToInsideEntitiesAmount.decreaseCount(removedComponent)
                if (insideEntities == 0) world.poolablesManager.poolableNeedsToBeReturnedToPool(removedComponent)
            }
            // if removed component is unique component, clear it's `inside entity` property
            if (removedComponent is UniqueComponent) {
                removedComponent.entityId = -1
            }
            // if component is Rich, remove id from plugged list
            else if (removedComponent is RichComponent) {
                removedComponent.insideEntitiesSet.remove(id)
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
            ComponentTypeToEntityPair(
                entityID = id,
                componentTypeId = componentTypeId,
            )
        )
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

    internal fun removeComponentSilently(id: Int) {
        val removedComponent = backingArray[id]
        backingArray[id] = null
        if (removedComponent != null) {
            // update component entity amount if it is poolable and request to remove it to the pool if amount is 0
            if (removedComponent is Poolable) {
                val insideEntities =
                    if (removedComponent is PoolableComponent) --removedComponent.insideEntities
                    else PoolableComponent.componentsToInsideEntitiesAmount.decreaseCount(removedComponent)
                if (insideEntities == 0) world.poolablesManager.poolableNeedsToBeReturnedToPool(removedComponent)
            }
            // if component is rich component, clear it's `inside entity` property
            if (removedComponent is UniqueComponent) {
                removedComponent.entityId = -1
            }
            // if component is Rich, remove id from plugged list
            else if (removedComponent is RichComponent) {
                removedComponent.insideEntitiesSet.remove(id)
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
                backingArray =  java.lang.reflect.Array.newInstance(type.java, initialSize) as Array<T?>,
                world = world,
                componentTypeId = componentTypeId
            )

        operator fun invoke(componentId: Int, world: World, initialSize: Int) =
            invoke(ExEcs.componentTypeIDsResolver.typeById(componentId), world, initialSize, componentId)
    }

}