package com.rdude.exECS.aspect

import com.rdude.exECS.component.State
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.utils.Dummies
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet

/**
 * Entities subscription can be shared between different systems with equal aspect.
 */
internal class EntitiesSubscription(aspect: Aspect) {

    // Entities to iterate through each world's iteration
    internal var entityIDs = IntIterableArray()

    // Fast way to check if this subscription is subscribed to an entity
    internal var hasEntities = UnsafeBitSet()

    // IDs of component types that are relevant for this subscription
    private var componentTypeIDs = UnsafeBitSet(ExEcs.componentTypeIDsResolver.size)

    // If presence of entities was not changed there is no need to remove unused entities
    private var hasRemoveRequests = false

    // Simple subscription is subscribed to only dummy entity
    private val isSimpleSubscription: Boolean

    // Aspect properties mapped to component type ids
    private val anyOfByType: IntIterableArray
    private val allOfByType: IntIterableArray
    private val excludeByType: IntIterableArray

    // States aspect properties
    private val anyOfByState: IterableArray<State>
    private val allOfByState: IterableArray<State>
    private val excludeByState: IterableArray<State>

    // Perform is entity match aspect checks, only on non-empty aspect entries
    private val entityMatchChecks: IterableArray<Check> = IterableArray()

    init {
        isSimpleSubscription = aspect.anyOf.isEmpty() && aspect.allOf.isEmpty()

        val anyOfTypeIds = aspect.anyOf.simpleComponents.map { ExEcs.componentTypeIDsResolver.idFor(it) }.toIntArray()
        anyOfByType = IntIterableArray(true, *anyOfTypeIds)
        anyOfTypeIds.forEach { componentTypeIDs[it] = true }

        anyOfByState = IterableArray(true, *aspect.anyOf.stateComponents.toTypedArray())
        anyOfByState.forEach { componentTypeIDs[it.getComponentTypeId()] = true }

        val allOfTypeIds = aspect.allOf.simpleComponents.map { ExEcs.componentTypeIDsResolver.idFor(it) }.toIntArray()
        allOfByType = IntIterableArray(true, *allOfTypeIds)
        allOfTypeIds.forEach { componentTypeIDs[it] = true }

        allOfByState = IterableArray(true, *aspect.allOf.stateComponents.toTypedArray())
        allOfByState.forEach { componentTypeIDs[it.getComponentTypeId()] = true }

        val excludeTypeIds = aspect.exclude.simpleComponents.map { ExEcs.componentTypeIDsResolver.idFor(it) }.toIntArray()
        excludeByType = IntIterableArray(true, *excludeTypeIds)
        excludeTypeIds.forEach { componentTypeIDs[it] = true }

        excludeByState = IterableArray(true, *aspect.exclude.stateComponents.toTypedArray())
        excludeByState.forEach { componentTypeIDs[it.getComponentTypeId()] = true }

        if (excludeByType.isNotEmpty()) entityMatchChecks.add(CheckExcludeType())
        if (excludeByState.isNotEmpty()) entityMatchChecks.add(CheckExcludeState())
        if (anyOfByType.isNotEmpty()) entityMatchChecks.add(CheckAnyOfType())
        if (anyOfByState.isNotEmpty()) entityMatchChecks.add(CheckAnyOfState())
        if (allOfByType.isNotEmpty()) entityMatchChecks.add(CheckAllOfType())
        if (allOfByState.isNotEmpty()) entityMatchChecks.add(CheckAllOfState())
    }

    fun isSubscribedToType(typeID: Int) = componentTypeIDs[typeID]

    fun isEntityMatchAspect(entityID: Int, entityMapper: EntityMapper): Boolean {
        if (isSimpleSubscription) {
            return entityID == Dummies.DUMMY_ENTITY_ID
        }
        for (entityMatchCheck in entityMatchChecks) {
            if (!entityMatchCheck.check(entityID, entityMapper)) return false
        }
        return true
    }

    fun setHasNotEntity(entityID: Int) {
        hasEntities.clear(entityID)
        hasRemoveRequests = true
    }

    fun removeUnusedEntities() {
        if (!hasRemoveRequests) return
        for (id in entityIDs) {
            if (!hasEntities[id]) {
                entityIDs.removeIteratingElement()
            }
        }
        hasRemoveRequests = false
    }

    fun instantlyRemoveEntity(id: Int) {
        hasEntities.clear(id)
        entityIDs.remove(id)
    }

    fun addEntity(id: Int) {
        entityIDs.add(id)
        hasEntities.set(id)
    }

    fun clearEntities() {
        entityIDs.clear()
        hasEntities.clear()
        hasRemoveRequests = false
    }


    private interface Check {
        fun check(entityID: Int, entityMapper: EntityMapper): Boolean
    }

    private inner class CheckExcludeType : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean {
            for (typeId in excludeByType) {
                if (entityMapper.componentMappers[typeId][entityID] != null) return false
            }
            return true
        }
    }

    private inner class CheckExcludeState : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean {
            for (state in excludeByState) {
                if (entityMapper.componentMappers[state.getComponentTypeId()][entityID] == state) return false
            }
            return true
        }
    }

    private inner class CheckAnyOfType : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean {
            for (typeId in anyOfByType) {
                if (entityMapper.componentMappers[typeId][entityID] != null) {
                    return true
                }
            }
            return false
        }
    }

    private inner class CheckAnyOfState : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean {
            for (state in anyOfByState) {
                if (entityMapper.componentMappers[state.getComponentTypeId()][entityID] == state) {
                    return true
                }
            }
            return false
        }
    }

    private inner class CheckAllOfType : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean {
            for (typeId in allOfByType) {
                if (entityMapper.componentMappers[typeId][entityID] == null) return false
            }
            return true
        }
    }

    private inner class CheckAllOfState : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean {
            for (state in allOfByState) {
                if (entityMapper.componentMappers[state.getComponentTypeId()][entityID] != state) return false
            }
            return true
        }
    }

}