package com.rdude.exECS.component

import com.rdude.exECS.component.state.ComponentStateManager
import com.rdude.exECS.component.state.componentStateManagerForType
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.exception.NoEntityException
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ComponentTypeToEntityPair
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

class ComponentMapper<T : Component> private constructor(
    private val world: World,
    @JvmField internal val stateManager: ComponentStateManager<*>?,
    @JvmField internal var backingArray: Array<T?>,
    @JvmField internal val componentTypeId: Int,
    @JvmField internal var sendComponentAddedEvents: Boolean = false,
    @JvmField internal var sendComponentRemovedEvents: Boolean = false
) {

    /** Entities with id less than this are SingletonEntities.
     * Used to update [SingletonEntity.componentsCache] property if needed.*/
    private val singletonEntitiesIdBoundary = ExEcs.singletonEntityIDsResolver.size

    operator fun get(id: Int): T? {
        try {
            return backingArray[id]
        }
        catch (e: IndexOutOfBoundsException) {
            if (id == Entity.NO_ENTITY.id) throw NoEntityException("Can not get a Component from Entity.NO_ENTITY")
            else throw e
        }
    }

    fun hasComponent(id: Int): Boolean {
        try {
            return backingArray[id] != null
        }
        catch (e: IndexOutOfBoundsException) {
            if (id == Entity.NO_ENTITY.id) throw NoEntityException("Can not check if Entity.NO_ENTITY has a Component")
            else throw e
        }
    }

    inline fun removeComponent(id: Int) = removeComponent(id, true)

    fun removeComponent(id: Int, eventAllowed: Boolean) {
        try {
            val removedComponent = backingArray[id] ?: return
            backingArray[id] = null
            // if removing from SingletonEntity, update its components cache
            if (id < singletonEntitiesIdBoundary) {
                world.entityMapper.singletons[id]?.componentsCache?.set(componentTypeId, null)
            }
            // update component states if needed
            stateManager?.componentRemovedUnsafe(removedComponent, id)
            // notify subscribersManager
            world.componentPresenceChange(ComponentTypeToEntityPair(entityID = id, componentTypeId = componentTypeId))
            // queue event
            if (eventAllowed && sendComponentRemovedEvents) {
                val event = ComponentRemovedEvent.pool.obtain()
                event.component = removedComponent
                event.entity = Entity(id)
                world.queueEvent(event)
            }
            // notify about internal change
            world.internalChangeOccurred = true
        }
        catch (e: IndexOutOfBoundsException) {
            if (id == Entity.NO_ENTITY.id) throw NoEntityException("Can not remove a Component from Entity.NO_ENTITY")
            else throw e
        }
    }

    inline fun addComponentUnsafe(id: Int, component: Component) = addComponentUnsafe(id, component, true)

    @Suppress("UNCHECKED_CAST")
    inline fun addComponentUnsafe(id: Int, component: Component, eventAllowed: Boolean) =
        addComponent(id, component as T, eventAllowed)

    inline fun addComponent(id: Int, component: T) = addComponent(id, component, true)

    fun addComponent(id: Int, component: T, eventAllowed: Boolean) {
        try {
            val removedComponent = backingArray[id]
            // if adding same component just return it
            if (removedComponent == component) return
            // if component was replaced update removed component states if needed
            if (removedComponent != null) {
                stateManager?.componentRemovedUnsafe(removedComponent, id)
            }
            // add component to the actual entity
            backingArray[id] = component
            // if adding to SingletonEntity, update its components cache
            if (id < singletonEntitiesIdBoundary) {
                world.entityMapper.singletons[id]?.componentsCache?.set(componentTypeId, component)
            }
            // update component states if needed
            stateManager?.componentAddedUnsafe(component, id)
            // notify subscribersManager
            world.componentPresenceChange(ComponentTypeToEntityPair(entityID = id, componentTypeId = componentTypeId))
            // queue events
            if (eventAllowed && sendComponentAddedEvents) {
                val event = ComponentAddedEvent.pool.obtain()
                event.component = component
                event.entity = Entity(id)
                event.replacedComponent = removedComponent
                world.queueEvent(event)
            }
            // notify about internal change
            world.internalChangeOccurred = true
        }
        catch (e: IndexOutOfBoundsException) {
            if (id == Entity.NO_ENTITY.id) throw NoEntityException("Can not add a Component to Entity.NO_ENTITY")
            else throw e
        }
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
            removeComponent(i, false)
        }
        world.internalChangeOccurred = true
    }

    internal fun grow(newSize: Int) {
        backingArray = backingArray.copyOf(newSize)
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