package com.rdude.exECS.entity

import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.utils.collections.IdArray
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.world.World

internal class EntityMapper(private var world: World) {

    // Stores all actual entities
    private var backingArray = IdArray<Entity>()

    // Indexes of the backing array cells that will be cleared after actualize() call
    // This array will be cleared only after all remove events fired
    private var removeRequests = IntIterableArray()

    // Indexes of the backing array containing entities not yet known to subscribers
    private var freshAddedEntities = IntIterableArray()

    // Subscriptions that need to be notified when entities are added or removed
    private var entitiesSubscriptions = IterableArray<EntitiesSubscription>()

    // Event pools. Events will be queued to the world's event bus at the end of actualize() call
    private val entityAddedEvents = Pool { EntityAddedEvent(world) }
    private val entityRemovedEvents = Pool { EntityRemovedEvent(world) }


    init {
        backingArray[EntityID.DUMMY_ENTITY_ID.id] = Entity.DUMMY_ENTITY
    }


    internal fun registerEntitiesSubscription(subscription: EntitiesSubscription) =
        entitiesSubscriptions.add(subscription)

    internal fun notifySubscriptionsManager() {
        val subscriptionsManager = world.subscriptionsManager
        subscriptionsManager.handleEntitiesAdded(freshAddedEntities)
        subscriptionsManager.handleEntitiesRemoved(removeRequests)
        subscriptionsManager.handleComponentPresenceChanges()
        subscriptionsManager.removeUnusedEntities()
    }


    fun create(components: Array<out Component>) {
        val entity = Entity.new(components)
        val id = backingArray.add(entity)
        // add to fresh entities
        freshAddedEntities.add(id)
        // queue event
        val event = entityAddedEvents.obtain()
        event.pureEntity = entity
        event.entityId = EntityID(id)
        world.queueInternalEvent(event)
    }

    fun requestRemove(id: EntityID) {
        removeRequests.add(id.id)
        val event = entityRemovedEvents.obtain()
        event.pureEntity = backingArray[id.id]!!
        event.entityId = id
        world.queueInternalEvent(event)
    }

    private fun remove(id: Int) {
        backingArray[id] = null
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
        backingArray.clear()
        removeRequests.clear()
        freshAddedEntities.clear()
        for (subscription in entitiesSubscriptions) {
            subscription.clearEntities()
        }
    }

    operator fun get(id: EntityID) : Entity = backingArray[id.id] as Entity

}