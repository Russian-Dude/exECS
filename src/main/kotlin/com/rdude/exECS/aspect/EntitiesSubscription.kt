package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentCondition
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.entity.EntityOrder
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.*
import com.rdude.exECS.utils.collections.EntitiesIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World

/** Stores the entities to which it is subscribed and the requirements for the entities that they must meet in order
 * to be subscribed to them.
 *
 * Entities subscription is shared between different [System]s with equal [Aspect] and [EntityOrder.Definition] in the same [World].*/
internal class EntitiesSubscription(
    world: World,
    aspect: Aspect,
    entityOrder: EntityOrder,
    initialCapacity: Int
) {

    private val componentMappers = world.entityMapper.componentMappers

    @JvmField internal val entities = EntitiesIterableArray(initialCapacity, entityOrder.comparator)

    private val componentTypeIDs = UnsafeBitSet(ExEcs.componentTypeIDsResolver.size)


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


    /** Subscribes, unsubscribes, or retains a subscription to the Entity, depending on whether it meets the requirements.*/
    fun updateSubscription(entityID: Int) {
        val entityMatch = checkEntityMatch(entityID)
        if (entityMatch) entities.add(entityID)
        else entities.requestRemove(entityID)
    }

    fun notifyChange(entityID: Int) {
        entities.changeOccurred(entityID)
    }

    /** Subscribes to the Entity if it is meets the requirements.*/
    fun tryToSubscribe(entityID: Int) {
        if (checkEntityMatch(entityID)) forceSubscribe(entityID)
    }

    /** Subscribes to the Entity without check whether it meets the requirements.*/
    fun forceSubscribe(entityId: Int) = entities.add(entityId)

    /** Marks Entity to be unsubscribed. It will be removed from the subscription at the next entities iteration of
     * any System that uses this subscription.*/
    fun markToUnsubscribe(entityId: Int) = entities.requestRemove(entityId)

    fun unsubscribeAll() = entities.clear()

    inline fun forEach(action: (Int) -> Unit) = entities.forEach(action)


    /** Checks whether the entity meets the requirements of this subscription.*/
    private inline fun checkEntityMatch(entityID: Int): Boolean =
        entityMatchChecks.all { it.check(entityID, componentMappers) }


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