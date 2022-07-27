package com.rdude.exECS.aspect

import com.rdude.exECS.component.*
import com.rdude.exECS.entity.EntityMapper
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

    /** This subscription can be subscribed to an entities contained in this [EntityMapper].*/
    private val entityMapper = world.entityMapper

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


    /** To be subscribed to an entity, it must have a state that equals to any of these states.*/
    private val anyOfByImmutableComponent: IterableArray<ImmutableComponent>

    /** To be subscribed to an entity, it must have states that equal to all of these states.*/
    private val allOfByImmutableComponent: IterableArray<ImmutableComponent>

    /** To be subscribed to an entity, it must not have a state that equals to any of these states.*/
    private val excludeByImmutableComponent: IterableArray<ImmutableComponent>


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

        anyOfByImmutableComponent = IterableArray(true, *aspect.anyOf.immutableComponents.toTypedArray())
        anyOfByImmutableComponent.forEach { componentTypeIDs[it.getComponentTypeId()] = true }

        anyOfComponentConditions = IterableArray(true, *aspect.anyOf.conditions.toTypedArray())
        anyOfComponentConditions.forEach { componentTypeIDs[it.componentClass.componentTypeId] = true }

        val allOfTypeIds = aspect.allOf.types.map { it.componentTypeId }.toIntArray()
        allOfByType = IntIterableArray(true, *allOfTypeIds)
        allOfTypeIds.forEach { componentTypeIDs[it] = true }

        allOfByImmutableComponent = IterableArray(true, *aspect.allOf.immutableComponents.toTypedArray())
        allOfByImmutableComponent.forEach { componentTypeIDs[it.getComponentTypeId()] = true }

        allOfComponentConditions = IterableArray(true, *aspect.allOf.conditions.toTypedArray())
        allOfComponentConditions.forEach { componentTypeIDs[it.componentClass.componentTypeId] = true }

        val excludeTypeIds = aspect.exclude.types.map { it.componentTypeId }.toIntArray()
        excludeByType = IntIterableArray(true, *excludeTypeIds)
        excludeTypeIds.forEach { componentTypeIDs[it] = true }

        excludeByImmutableComponent = IterableArray(true, *aspect.exclude.immutableComponents.toTypedArray())
        excludeByImmutableComponent.forEach { componentTypeIDs[it.getComponentTypeId()] = true }

        excludeComponentConditions = IterableArray(true, *aspect.exclude.conditions.toTypedArray())
        excludeComponentConditions.forEach { componentTypeIDs[it.componentClass.componentTypeId] = true }

        if (excludeByType.isNotEmpty()) entityMatchChecks.add(CheckExcludeType())
        if (excludeByImmutableComponent.isNotEmpty()) entityMatchChecks.add(CheckExcludeState())
        if (excludeComponentConditions.isNotEmpty()) entityMatchChecks.add(CheckExcludeComponentCondition())
        if (anyOfByType.isNotEmpty()) entityMatchChecks.add(CheckAnyOfType())
        if (anyOfByImmutableComponent.isNotEmpty()) entityMatchChecks.add(CheckAnyOfState())
        if (anyOfComponentConditions.isNotEmpty()) entityMatchChecks.add(CheckAnyOfComponentCondition())
        if (allOfByType.isNotEmpty()) entityMatchChecks.add(CheckAllOfType())
        if (allOfByImmutableComponent.isNotEmpty()) entityMatchChecks.add(CheckAllOfState())
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
    fun checkEntityMatch(entityID: Int): Boolean = entityMatchChecks.all { it.check(entityID, entityMapper) }


    private interface Check {
        fun check(entityID: Int, entityMapper: EntityMapper): Boolean
    }

    private inner class CheckExcludeType : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            excludeByType.none { entityMapper.componentMappers[it][entityID] != null }
    }

    private inner class CheckExcludeState : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            excludeByImmutableComponent.none { entityMapper.componentMappers[it.getComponentTypeId()][entityID] == it }
    }

    private inner class CheckExcludeComponentCondition : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            excludeComponentConditions.none {
                val component = entityMapper.componentMappers[it.componentClass.componentTypeId][entityID]
                if (component == null) false else it.unsafeTest(component)
            }
    }

    private inner class CheckAnyOfType : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            anyOfByType.any { entityMapper.componentMappers[it][entityID] != null }
    }

    private inner class CheckAnyOfState : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            anyOfByImmutableComponent.any { entityMapper.componentMappers[it.getComponentTypeId()][entityID] == it }
    }

    private inner class CheckAnyOfComponentCondition : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            anyOfComponentConditions.any {
                val component = entityMapper.componentMappers[it.componentClass.componentTypeId][entityID]
                if (component == null) false else it.unsafeTest(component)
            }
    }

    private inner class CheckAllOfType : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            allOfByType.all { entityMapper.componentMappers[it][entityID] != null }
    }

    private inner class CheckAllOfComponentCondition : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            allOfComponentConditions.all {
                val component = entityMapper.componentMappers[it.componentClass.componentTypeId][entityID]
                if (component == null) false else it.unsafeTest(component)
            }
    }

    private inner class CheckAllOfState : Check {
        override fun check(entityID: Int, entityMapper: EntityMapper): Boolean =
            allOfByImmutableComponent.all { entityMapper.componentMappers[it.getComponentTypeId()][entityID] == it }
    }


    // both methods below used to hack compiler errors
    private inline fun ComponentCondition<*>.unsafeTest(component: Component): Boolean {
        component as ObservableComponent<*>?
        component as CanBeObservedBySystem?
        return component.unsafeTestByCondition(this)
    }

    private inline fun <T> T.unsafeTestByCondition(condition: ComponentCondition<*>): Boolean where T : ObservableComponent<*>, T : CanBeObservedBySystem {
        condition as ComponentCondition<T>
        return condition.test(this)
    }

}