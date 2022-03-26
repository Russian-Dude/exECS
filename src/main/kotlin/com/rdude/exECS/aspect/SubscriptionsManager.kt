package com.rdude.exECS.aspect

import com.rdude.exECS.component.ComponentPresenceChange
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.LongIterableArray
import com.rdude.exECS.world.World

internal class SubscriptionsManager(val world: World) {

    private val subscriptions = IterableArray<EntitiesSubscription>()
    private val componentPresenceChanges = LongIterableArray()

    internal fun add(subscription: EntitiesSubscription) = subscriptions.add(subscription)

    internal fun componentPresenceChange(change: ComponentPresenceChange) = componentPresenceChanges.add(change.data)

    internal fun handleEntitiesAdded(entities: IntIterableArray) {
        for (id in entities) {
            for (subscription in subscriptions) {
                val entityMatchAspect = subscription.isEntityMatchAspect(id, world.entityMapper)
                if (entityMatchAspect) {
                    subscription.addEntity(id)
                }
            }
        }
    }

    internal fun handleEntitiesRemoved(entities: IntIterableArray) {
        for (entityID in entities) {
            for (subscription in subscriptions) {
                subscription.setHasNotEntity(entityID)
            }
        }
    }

    internal fun handleComponentPresenceChanges() {
        for (c in componentPresenceChanges) {
            val change = ComponentPresenceChange(c)
            val entityID = change.entityId()
            val componentTypeID = change.componentId()
            for (subscription in subscriptions) {
                if (subscription.isSubscribedToType(componentTypeID)) {
                    val entityMatchAspect = subscription.isEntityMatchAspect(entityID, world.entityMapper)
                    val hasEntity = subscription.hasEntities[entityID]
                    if (!entityMatchAspect && hasEntity) {
                        subscription.setHasNotEntity(entityID)
                    }
                    else if (entityMatchAspect && !hasEntity) {
                        subscription.addEntity(entityID)
                    }
                }
            }
        }
        componentPresenceChanges.clear()
    }

    internal fun removeUnusedEntities() {
        for (subscription in subscriptions) {
            subscription.removeUnusedEntities()
        }
    }

}