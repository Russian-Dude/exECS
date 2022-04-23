package com.rdude.exECS.entity

import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.IntArrayStackSet
import com.rdude.exECS.utils.collections.IntIterableArray
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

    // Amount of IDs reserved for singletons
    private val reservedForSingletons = ExEcs.singletonEntityIDsResolver.typesAmount

    // Singleton instances
    internal val singletons: Array<SingletonEntity?> = Array(reservedForSingletons) { null }

    // Next free ID. Initially: dummy entity + reserved IDs for singletons
    internal var nextID: Int = 1 + reservedForSingletons

    // Current entities amount. Initially: dummy entity
    internal var size = 1

    // Grow bitsets at the same time as component mappers will grow
    private val linkedBitSets = IterableArray<UnsafeBitSet>()

    // Ids that can be reused
    private val emptyIds = IntArrayStackSet()

    // Indexes of the backing array cells that will be cleared after actualize() call
    // This array will be cleared only after all remove events fired
    private val removeRequests = IntArrayStackSet()

    // backing array indexes that subscribers don't know about removing yet
    private val freshRemovedEntities = IntArrayStackSet()

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
        subscriptionsManager.handleEntitiesRemoved(freshRemovedEntities)
        subscriptionsManager.handleComponentPresenceChanges()
        subscriptionsManager.removeUnusedEntities()
        freshRemovedEntities.clear()
        freshAddedEntities.clear()
        world.subscriptionsNeedToBeUpdated = false
    }

    // should only be called between world act calls
    internal fun rearrange() {
        if (emptyIds.isEmpty()) return
        var lastElementIndex = nextID - 1
        for (emptyId in emptyIds.sortedDescending()) {
            if (lastElementIndex < reservedForSingletons) {
                break
            }
            if (lastElementIndex == emptyId) {
                lastElementIndex--
                continue
            }
            for (componentMapper in componentMappers) {
                componentMapper.replaceId(lastElementIndex, emptyId)
            }
            world.subscriptionsManager.moveEntity(lastElementIndex, emptyId)
            lastElementIndex--
        }
        nextID = lastElementIndex + 1
        emptyIds.clear()
    }

    fun create(components: Array<out Component>) {
        val id = if (emptyIds.isNotEmpty()) emptyIds.unsafePoll() else nextID
        if (id == nextID) nextID++ // can poll nextID from empty ids
        size++
        // grow growables if needed
        if (nextID >= componentMappersSize) {
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
        event.entityAsSingleton = null
        world.queueInternalEvent(event)
        world.internalChangeOccurred = true
    }

    fun addSingletonEntity(singletonEntity: SingletonEntity) {
        val entityID = singletonEntity.entityID
        if (singletons[entityID] != null) return
        singletons[entityID] = singletonEntity
        size++
        // add to fresh entities
        freshAddedEntities.add(entityID)
        // queue event
        val event = EntityAddedEvent.pool.obtain()
        event.entity = EntityWrapper(entityID)
        event.entityAsSingleton = singletonEntity
        world.queueInternalEvent(event)
        world.internalChangeOccurred = true
    }

    fun requestRemove(id: Int) {
        val requestAdded = removeRequests.add(id)
        if (!requestAdded) return
        freshRemovedEntities.add(id)
        val event = EntityRemovedEvent.pool.obtain()
        event.entity = EntityWrapper(id)
        event.entityAsSingleton = if (id >= reservedForSingletons) null else singletons[id]
        world.queueInternalEvent(event)
        world.internalChangeOccurred = true
    }

    private fun remove(id: Int) {
        size--
        val isNotSingleton = id >= reservedForSingletons
        if (isNotSingleton) {
            emptyIds.add(id)
        }
        if (isNotSingleton) {
            componentMappers.forEach { it.removeComponentSilently(id) }
        }
        else {
            singletons[id] = null
        }
    }

    fun actualize() {
        // remove requests
        for (removeRequest in removeRequests) {
            remove(removeRequest)
        }
        removeRequests.clear()
    }

    fun clear() {
        componentMappers.forEach { it.clear() }
        removeRequests.clear()
        freshAddedEntities.clear()
        freshRemovedEntities.clear()
        for (subscription in entitiesSubscriptions) {
            subscription.clearEntities()
        }
        world.internalChangeOccurred = true
    }

}

