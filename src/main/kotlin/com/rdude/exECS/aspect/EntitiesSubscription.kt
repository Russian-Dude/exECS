package com.rdude.exECS.aspect

import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.utils.Dummies
import com.rdude.exECS.utils.collections.IntIterableArray
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
    private var componentTypeIDs = UnsafeBitSet(ComponentTypeIDsResolver.size)

    // If presence of entities was not changed there is no need to remove unused entities
    private var hasRemoveRequests = false

    // Aspect properties mapped to component type ids
    internal val anyOf: IntIterableArray
    internal val allOf: IntIterableArray
    internal val exclude: IntIterableArray

    init {
        if (aspect.anyOf.isEmpty() && aspect.allOf.isEmpty()) {
            entityIDs.add(Dummies.DUMMY_ENTITY_ID)
            hasEntities[Dummies.DUMMY_ENTITY_ID] = true
        }

        val anyOfTypeIds = aspect.anyOf.map { ComponentTypeIDsResolver.idFor(it) }.toIntArray()
        anyOf = IntIterableArray(true, *anyOfTypeIds)
        anyOfTypeIds.forEach { componentTypeIDs[it] = true }

        val allOfTypeIds = aspect.allOf.map { ComponentTypeIDsResolver.idFor(it) }.toIntArray()
        allOf = IntIterableArray(true, *allOfTypeIds)
        allOfTypeIds.forEach { componentTypeIDs[it] = true }

        val excludeTypeIds = aspect.exclude.map { ComponentTypeIDsResolver.idFor(it) }.toIntArray()
        exclude = IntIterableArray(true, *excludeTypeIds)
        excludeTypeIds.forEach { componentTypeIDs[it] = true }
    }

    fun isSubscribedToType(typeID: Int) = componentTypeIDs[typeID]

    fun isEntityMatchAspect(entityID: Int, entityMapper: EntityMapper): Boolean {
        for (typeId in exclude) {
            if (entityMapper.componentMappers[typeId][entityID] != null) return false
        }
        var hasAnyOf = false
        for (typeId in anyOf) {
            if (entityMapper.componentMappers[typeId][entityID] != null) {
                hasAnyOf = true
                break
            }
        }
        if (!hasAnyOf && anyOf.isNotEmpty()) return false
        for (typeId in allOf) {
            if (entityMapper.componentMappers[typeId][entityID] == null) return false
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

}