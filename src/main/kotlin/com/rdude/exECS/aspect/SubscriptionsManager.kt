package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.*
import com.rdude.exECS.world.World

internal class SubscriptionsManager(private val world: World, freshAddedEntitiesArray: IntIterableArray, freshRemovedEntitiesArray: IntArrayStackSet) {

    /** All subscriptions.*/
    private val subscriptions = IterableArray<EntitiesSubscription>()

    /** Subscriptions grouped by component type ids.*/
    private val subscriptionsByComponentType = Array(ExEcs.componentTypeIDsResolver.size) { IterableArray<EntitiesSubscription>() }

    /** Changes of components (added, removed, observable changed).
     *  Based on these changes, subscriptions can subscribe or unsubscribe from entities.
     *  Long values in array are [ComponentTypeToEntityPair].*/
    private val componentsChanges = LongIterableArray()

    /** Entities that were added during the [World.act] call.
     *  This array is shared with [EntityMapper] which populates its contents ([EntityMapper.freshAddedEntities]).*/
    private val freshAddedEntities: IntIterableArray = freshAddedEntitiesArray

    /** Entities that were removed during the [World.act] call.
     *  This array is shared with [EntityMapper] which populates its contents ([EntityMapper.freshRemovedEntities]).*/
    private val freshRemovedEntities: IntArrayStackSet = freshRemovedEntitiesArray

    /** [updateSubscriptions] will perform an update only when this property is true.*/
    @JvmField internal var updateRequired = false


    /** Sets either an existing suitable [EntitiesSubscription] for [system] or creates a new one.*/
    internal fun registerSystem(system: System) {
        var subscriptionCopied = false
        world.systems.forEach { otherSystem ->
            if (otherSystem !== system && system.aspect == otherSystem.aspect) {
                system.entitiesSubscription = otherSystem.entitiesSubscription
                subscriptionCopied = true
                return@forEach
            }
        }
        if (!subscriptionCopied) {
            val subscription = EntitiesSubscription(world, system.aspect)
            for (entityId in 0 until world.entityMapper.nextID) {
                subscription.tryToSubscribe(entityId)
            }
            system.entitiesSubscription = subscription
            world.entityMapper.linkEntityBitSet(system.entitiesSubscription.hasEntities)
            subscriptions.add(subscription)
            subscription.componentTypeIDs.getTrueValues().forEach { subscriptionsByComponentType[it].add(subscription) }
        }
    }


    /** Stores the type id of the changed [Component] and the entity id this component is plugged into
     * to later update [EntitiesSubscription]s depending on the component changes.*/
    internal fun componentChanged(change: ComponentTypeToEntityPair) = componentsChanges.add(change.data)


    /** Updates subscriptions depending on changes to components and entities.
     * Updates only when [updateRequired] is true.*/
    internal fun updateSubscriptions() {
        if (!updateRequired) return
        // entities added
        freshAddedEntities.forEach { id ->
            subscriptions.forEach { subscription ->
                subscription.tryToSubscribe(id)
            }
        }
        // entities removed
        freshRemovedEntities.forEach { entityId ->
            subscriptions.forEach { subscription ->
                subscription.markToUnsubscribe(entityId)
            }
        }
        // component changes
        for (c in componentsChanges) {
            val change = ComponentTypeToEntityPair(c)
            val entityID = change.entityId()
            val componentTypeID = change.componentId()
            subscriptionsByComponentType[componentTypeID]
                .forEach { it.updateSubscription(entityID) }
        }
        // final
        componentsChanges.clear()
        subscriptions.forEach { it.unsubscribeMarked() }
        freshRemovedEntities.clear()
        freshAddedEntities.clear()
        updateRequired = false
    }

    internal fun entityChangedId(fromId: Int, toId: Int) {
        subscriptions.forEach { subscription ->
            if (subscription.hasEntities[fromId]) {
                subscription.forceUnsubscribe(fromId)
                subscription.forceSubscribe(toId)
            }
        }
    }

    /** Unsubscribe all subscriptions from all entities.*/
    internal fun unsubscribeAll() {
        subscriptions.forEach { it.unsubscribeAll() }
    }

}