package com.rdude.exECS.entity

import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.*
import com.rdude.exECS.utils.collections.ArrayStack
import com.rdude.exECS.utils.collections.IntArrayStack
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.world.World

internal class EntityMapper(private var world: World) {

    // Current backing array size of component mappers
    // Stored here to calculate new size only once and only when entity with id that exceeds current size is added
    internal var componentMappersSize: Int = 16

    // Stores component mappers for every component type. Array index - component type id
    internal val componentMappers: Array<ComponentMapper<*>> =
        Array(ExEcs.componentTypeIDsResolver.size) { ComponentMapper(it, world, componentMappersSize) }

    // Current amount of entities
    internal var size: Int = 1

    // Grow bitsets at the same time as component mappers will grow
    private val linkedBitSets = IterableArray<UnsafeBitSet>()

    // Ids that can be reused
    private val emptyIds = IntArrayStack()

    // Indexes of the backing array cells that will be cleared after actualize() call
    // This array will be cleared only after all remove events fired
    private var removeRequests = IntIterableArray()

    // Indexes of the backing array containing entities not yet known to subscribers
    private var freshAddedEntities = IntIterableArray()

    // Subscriptions that need to be notified when entities are added or removed
    private var entitiesSubscriptions = IterableArray<EntitiesSubscription>()


    internal fun registerEntitiesSubscription(subscription: EntitiesSubscription) {
        entitiesSubscriptions.add(subscription)
        linkedBitSets.add(subscription.hasEntities)
    }

    internal fun notifySubscriptionsManager() {
        val subscriptionsManager = world.subscriptionsManager
        subscriptionsManager.handleEntitiesAdded(freshAddedEntities)
        subscriptionsManager.handleEntitiesRemoved(removeRequests)
        subscriptionsManager.handleComponentPresenceChanges()
        subscriptionsManager.removeUnusedEntities()
    }

    // should only be called between world act calls
    internal fun rearrange() {
        if (emptyIds.isEmpty()) return
        var lastElementIndex = size + emptyIds.size - 1
        for (emptyId in emptyIds) {
            for (componentMapper in componentMappers) {
                componentMapper.replaceId(lastElementIndex, emptyId)
            }
            world.subscriptionsManager.moveEntity(lastElementIndex, emptyId)
            lastElementIndex--
        }
        emptyIds.clear()
    }

    fun create(components: Array<out Component>) {
        val id = if (emptyIds.isNotEmpty()) emptyIds.unsafePoll() else size
        size++
        // grow growables if needed
        if (size >= componentMappersSize) {
            componentMappersSize *= 2
            componentMappers.forEach { it.grow(componentMappersSize) }
            linkedBitSets.forEach { it.growIfNeeded(componentMappersSize) }
        }
        // add components to component mappers
        components.forEach {
            componentMappers[it.getComponentTypeId()].unsafeSet(id, it)
        }
        // add to fresh entities
        freshAddedEntities.add(id)
        // queue event
        val event = EntityAddedEvent.pool.obtain()
        event.entity = EntityWrapper(id)
        world.queueInternalEvent(event)
        world.internalChangeOccurred = true
    }

    fun requestRemove(id: Int) {
        removeRequests.add(id)
        val event = EntityRemovedEvent.pool.obtain()
        event.entity = EntityWrapper(id)
        world.queueInternalEvent(event)
        world.internalChangeOccurred = true
    }

    private fun remove(id: Int) {
        size--
        emptyIds.add(id)
        componentMappers.forEach { it.removeComponentSilently(id) }
    }

    fun actualize() {
        // remove requests
        for (removeRequest in removeRequests) {
            remove(removeRequest)
        }
        removeRequests.clear()
        // fresh added entities
        freshAddedEntities.clear()
    }

    fun clear() {
        componentMappers.forEach { it.clear() }
        removeRequests.clear()
        freshAddedEntities.clear()
        for (subscription in entitiesSubscriptions) {
            subscription.clearEntities()
        }
        world.internalChangeOccurred = true
    }

}

