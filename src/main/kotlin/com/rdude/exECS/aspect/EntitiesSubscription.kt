package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentCondition
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World

/** Stores the entities to which it is subscribed and the requirements for the entities that they must meet in order
 * to be subscribed to them.
 *
 * Entities subscription is shared between different [System]s with equal [Aspect] in the same [World].*/
internal class EntitiesSubscription(world: World, aspect: Aspect) {

    private val componentMappers = world.entityMapper.componentMappers

    /** Entities ids to which this instance is subscribed.*/
    @JvmField internal var entityIDs = IntIterableArray()

    /** Fast way to check if this subscription is subscribed to an entity.*/
    @JvmField internal var hasEntities = UnsafeBitSet()

    /** IDs of component types that are relevant for this subscription.*/
    @JvmField internal var componentTypeIDs = UnsafeBitSet(ExEcs.componentTypeIDsResolver.size)

    /** Marking that at least one entity has been removed from the [World] and must be unsubscribed.*/
    private var hasRemoveRequests = false


    /** To be subscribed to an entity, it must have any of these component types (by id).*/
    private val anyOfByType: IntIterableArray

    /** To be subscribed to an entity, it must have all of these component types (by id).*/
    private val allOfByType: IntIterableArray

    /** To be subscribed to an entity, it must have none of these component types (by id).*/
    private val excludeByType: IntIterableArray


    /** To be subscribed to an entity, it must have a component that matches any of these conditions.*/
    private val anyOfComponentConditions: IterableArray<ComponentCondition<*>>

    /** To be subscribed to an entity, it must have components that match all of these conditions.*/
    private val allOfComponentConditions: IterableArray<ComponentCondition<*>>

    /** To be subscribed to an entity, it must not have any component that matches any of these conditions.*/
    private val excludeComponentConditions: IterableArray<ComponentCondition<*>>


    /** The checks that need to be made to determine if an entity can be subscribed to by this subscription.*/
    private val entityMatchChecks: IterableArray<Check> = IterableArray()

    init {

        val anyOfTypeIds = aspect.anyOf.types.map { it.componentTypeId }.toIntArray()
        anyOfByType = IntIterableArray(true, *anyOfTypeIds)
        anyOfTypeIds.forEach { componentTypeIDs[it] = true }

        anyOfComponentConditions = IterableArray(true, *aspect.anyOf.conditions.toTypedArray())
        anyOfComponentConditions.forEach { componentTypeIDs[it.componentTypeId] = true }

        val allOfTypeIds = aspect.allOf.types.map { it.componentTypeId }.toIntArray()
        allOfByType = IntIterableArray(true, *allOfTypeIds)
        allOfTypeIds.forEach { componentTypeIDs[it] = true }

        allOfComponentConditions = IterableArray(true, *aspect.allOf.conditions.toTypedArray())
        allOfComponentConditions.forEach { componentTypeIDs[it.componentTypeId] = true }

        val excludeTypeIds = aspect.exclude.types.map { it.componentTypeId }.toIntArray()
        excludeByType = IntIterableArray(true, *excludeTypeIds)
        excludeTypeIds.forEach { componentTypeIDs[it] = true }

        excludeComponentConditions = IterableArray(true, *aspect.exclude.conditions.toTypedArray())
        excludeComponentConditions.forEach { componentTypeIDs[it.componentTypeId] = true }

        if (excludeByType.isNotEmpty()) entityMatchChecks.add(CheckExcludeType())
        if (excludeComponentConditions.isNotEmpty()) entityMatchChecks.add(CheckExcludeComponentCondition())
        if (anyOfByType.isNotEmpty()) entityMatchChecks.add(CheckAnyOfType())
        if (anyOfComponentConditions.isNotEmpty()) entityMatchChecks.add(CheckAnyOfComponentCondition())
        if (allOfByType.isNotEmpty()) entityMatchChecks.add(CheckAllOfType())
        if (allOfComponentConditions.isNotEmpty()) entityMatchChecks.add(CheckAllOfComponentCondition())
    }


    /** Subscribes, marks to unsubscribe, or retains a subscription to the entity, depending on whether the entity meets the requirements.*/
    fun updateSubscription(entityID: Int) {
        val entityMatch = checkEntityMatch(entityID)
        val hasEntity = hasEntities[entityID]
        if (!entityMatch && hasEntity) {
            markToUnsubscribe(entityID)
        } else if (entityMatch && !hasEntity) {
            forceSubscribe(entityID)
        }
    }

    /** Marks the entity to be unsubscribed from this subscription.
     * To finally unsubscribe marked entities [unsubscribeMarked] method should be called.
     * In order to instantly unsubscribe the entity [forceUnsubscribe] method can be called instead.*/
    fun markToUnsubscribe(entityId: Int) {
        hasEntities.clear(entityId)
        hasRemoveRequests = true
    }

    /** Unsubscribes from the entities that are marked to be unsubscribed.*/
    fun unsubscribeMarked() {
        if (!hasRemoveRequests) return
        entityIDs.removeIf { !hasEntities[it] }
        hasRemoveRequests = false
    }

    /** Instantly unsubscribe from the entity.*/
    fun forceUnsubscribe(entityId: Int) {
        hasEntities.clear(entityId)
        entityIDs.remove(entityId)
    }

    /** Subscribes to the entity if it is meets the requirements.*/
    fun tryToSubscribe(entityID: Int) {
        if (checkEntityMatch(entityID)) forceSubscribe(entityID)
    }

    /** Subscribes to the entity without check whether the entity meets the requirements.*/
    fun forceSubscribe(entityId: Int) {
        entityIDs.add(entityId)
        hasEntities.set(entityId)
    }

    /** Unsubscribes from all entities.*/
    fun unsubscribeAll() {
        entityIDs.clear()
        hasEntities.clear()
        hasRemoveRequests = false
    }

    /** Checks whether the entity meets the requirements of this subscription.*/
    fun checkEntityMatch(entityID: Int): Boolean = entityMatchChecks.all { it.check(entityID, componentMappers) }


    private interface Check {
        fun check(entityID: Int, componentMappers: Array<ComponentMapper<*>>): Boolean
    }

    private inner class CheckExcludeType : Check {
        override fun check(entityID: Int, componentMappers: Array<ComponentMapper<*>>): Boolean =
            excludeByType.none { componentMappers[it][entityID] != null }
    }

    private inner class CheckExcludeComponentCondition : Check {
        @Suppress("UNCHECKED_CAST")
        override fun check(entityID: Int, componentMappers: Array<ComponentMapper<*>>): Boolean =
            excludeComponentConditions.none {
                val component = componentMappers[it.componentTypeId][entityID]
                if (component == null) false else (it as ComponentCondition<Component>).test(component)
            }
    }

    private inner class CheckAnyOfType : Check {
        override fun check(entityID: Int, componentMappers: Array<ComponentMapper<*>>): Boolean =
            anyOfByType.any { componentMappers[it][entityID] != null }
    }

    private inner class CheckAnyOfComponentCondition : Check {
        @Suppress("UNCHECKED_CAST")
        override fun check(entityID: Int, componentMappers: Array<ComponentMapper<*>>): Boolean =
            anyOfComponentConditions.any {
                val component = componentMappers[it.componentTypeId][entityID]
                if (component == null) false else (it as ComponentCondition<Component>).test(component)
            }
    }

    private inner class CheckAllOfType : Check {
        override fun check(entityID: Int, componentMappers: Array<ComponentMapper<*>>): Boolean =
            allOfByType.all { componentMappers[it][entityID] != null }
    }

    private inner class CheckAllOfComponentCondition : Check {
        @Suppress("UNCHECKED_CAST")
        override fun check(entityID: Int, componentMappers: Array<ComponentMapper<*>>): Boolean =
            allOfComponentConditions.all {
                val component = componentMappers[it.componentTypeId][entityID]
                if (component == null) false else (it as ComponentCondition<Component>).test(component)
            }
    }

}