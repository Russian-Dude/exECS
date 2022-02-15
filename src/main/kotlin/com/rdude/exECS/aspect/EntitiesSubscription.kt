package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentTypeID
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet
import kotlin.reflect.KClass

/**
 * Entities subscription can be shared between different systems with equal aspect.
 */
internal class EntitiesSubscription(aspect: Aspect) {

    // Entities to iterate through each world's iteration
    internal var entityIDs = IntIterableArray()

    // Fast way to check if this subscription is subscribed to an entity
    internal var hasEntities = UnsafeBitSet()

    // IDs of component types that are relevant for this subscription
    private var componentTypeIDs = UnsafeBitSet(ComponentTypeIDsResolver.maxIndex)

    // If presence of entities was not changed there is no need to remove unused entities
    private var hasRemoveRequests = false

    // Aspect properties
    internal val anyOf: IterableArray<KClass<out Component>>
    internal val allOf: IterableArray<KClass<out Component>>
    internal val exclude: IterableArray<KClass<out Component>>

    init {
        if (aspect.anyOf.isEmpty() && aspect.allOf.isEmpty()) {
            entityIDs.add(EntityID.DUMMY_ENTITY_ID.id)
            hasEntities[0] = true
        }
        anyOf = IterableArray(true, *aspect.anyOf.toTypedArray())
        for (componentClass in aspect.anyOf) {
            val id = ComponentTypeIDsResolver.idFor(componentClass).id
            componentTypeIDs[id] = true
        }
        allOf = IterableArray(true, *aspect.allOf.toTypedArray())
        for (componentClass in aspect.allOf) {
            val id = ComponentTypeIDsResolver.idFor(componentClass).id
            componentTypeIDs[id] = true
        }
        exclude = IterableArray(true, *aspect.exclude.toTypedArray())
        for (componentClass in aspect.exclude) {
            val id = ComponentTypeIDsResolver.idFor(componentClass).id
            componentTypeIDs[id] = true
        }
    }

    fun isSubscribedToType(typeID: ComponentTypeID) = componentTypeIDs[typeID.id]

    fun isEntityMatchAspect(entity: Entity): Boolean {
        for (type in exclude) {
            if (entity.hasComponent(type)) return false
        }
        var hasAnyOf = false
        for (type in anyOf) {
            if (entity.hasComponent(type)) {
                hasAnyOf = true
                break
            }
        }
        if (!hasAnyOf && anyOf.isNotEmpty()) return false
        for (type in allOf) {
            if (!entity.hasComponent(type)) return false
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

    fun instantlyRemoveEntity(id: EntityID) {
        hasEntities.clear(id.id)
        entityIDs.remove(id.id)
    }

    fun addEntity(id: EntityID) {
        entityIDs.add(id.id)
        hasEntities.set(id.id)
    }

    fun clearEntities() {
        entityIDs.clear()
        hasEntities.clear()
        hasRemoveRequests = false
    }

}