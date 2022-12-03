package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.entity.EntityOrder
import com.rdude.exECS.system.IterableEventSystem
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.*
import com.rdude.exECS.utils.fastForEach
import com.rdude.exECS.world.World

internal class SubscriptionsManager(
    private val world: World,
    freshAddedEntitiesArray: IntIterableArray,
    freshRemovedEntitiesArray: IntArrayStackSet,
    private val initialEntitiesCapacity: Int
) {

    /** All subscriptions.*/
    private val subscriptions = IterableArray<EntitiesSubscription>()

    /** Subscriptions grouped by [Aspect] component type ids.*/
    private val subscriptionsByAspectComponentType = Array(ExEcs.componentTypeIDsResolver.size) { IterableArray<EntitiesSubscription>() }

    /** Subscriptions grouped by [EntityOrder] component type ids. */
    private val subscriptionsByOrderComponentType = Array(ExEcs.componentTypeIDsResolver.size) { IterableArray<EntitiesSubscription>() }

    /** Changes of components (added, removed, observable changed).
     *  Based on these changes, subscriptions can subscribe or unsubscribe from entities or perform sorting.
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


    /** Sets either an existing suitable [EntitiesSubscription] for the [system] or creates a new one.*/
    internal fun registerSystem(system: IterableEventSystem<*>) {
        world.systems.fastForEach { otherSystem ->
            if (otherSystem !== system
                && otherSystem is IterableEventSystem<*>
                && system.entityOrder.orderDefinition == otherSystem.entityOrder.orderDefinition
                && system.aspect == otherSystem.aspect
            ) {
                system.entitiesSubscription = otherSystem.entitiesSubscription
                return
            }
        }

        val subscription = EntitiesSubscription(world, system.aspect, system.entityOrder, initialEntitiesCapacity)
        for (entityId in 0 until world.entityMapper.nextID) {
            subscription.tryToSubscribe(entityId)
        }
        system.entitiesSubscription = subscription
        world.entityMapper.linkEntityBitSet(subscription.entities.presence)
        world.entityMapper.linkEntityBitSet(subscription.entities.markedToRemove)
        subscriptions.add(subscription)

        system.aspect.getComponentTypeIds().forEach {
            subscriptionsByAspectComponentType[it].add(subscription)
        }
        system.entityOrder.dependsOnComponentTypes.forEach {
            subscriptionsByOrderComponentType[it].add(subscription)
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
            subscriptionsByAspectComponentType[componentTypeID]
                .forEach { it.updateSubscription(entityID) }
            subscriptionsByOrderComponentType[componentTypeID]
                .forEach { it.notifyChange(entityID) }
        }
        // final
        componentsChanges.clear()
        freshRemovedEntities.clear()
        freshAddedEntities.clear()
        updateRequired = false
    }

    internal fun entityChangedId(fromId: Int, toId: Int) {
        subscriptions.forEach { subscription ->
            val entities = subscription.entities
            if (entities.presence[fromId]) {
                entities.requestRemove(fromId)
                entities.add(toId)
            }
        }
    }

    /** Unsubscribe all subscriptions from all entities.*/
    internal fun unsubscribeAll() {
        subscriptions.forEach { it.unsubscribeAll() }
        componentsChanges.clear()
    }

}