package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.IntArrayStackSet
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.world.World
import com.rdude.exECS.aspect.SubscriptionsManager

internal class EntityMapper(private var world: World, freshAddedEntitiesArray: IntIterableArray, freshRemovedEntitiesArray: IntArrayStackSet) {

    /** Current backing array size of component mappers
     * Stored here to calculate new size only once and only when entity with id that exceeds current size is added.*/
    @JvmField internal var componentMappersSize: Int = 16

    /** Stores component mappers for every component type. Array index - component type id.*/
    @JvmField internal val componentMappers: Array<ComponentMapper<*>> =
        Array(ExEcs.componentTypeIDsResolver.size) { ComponentMapper(it, world, componentMappersSize) }

    /** Amount of IDs reserved for singletons.*/
    private val reservedForSingletons = ExEcs.singletonEntityIDsResolver.typesAmount

    /** Singleton instances.*/
    @JvmField internal val singletons: Array<SingletonEntity?> = Array(reservedForSingletons + 1) { null }

    /** Next free ID. Initially: dummy entity + reserved IDs for singletons.*/
    @JvmField internal var nextID: Int = 1 + reservedForSingletons

    /** Current entities amount. Initially: dummy entity.*/
    @JvmField internal var size = 1

    /** [UnsafeBitSet] does not contain logic to increase it backing array in order to reduce checks every time its
     *  value is accessed. Since the size of [UnsafeBitSet]s containing information about entities directly depends on
     *  the number of entities, and hence on the size of EntityMapper, the size of their backing arrays can be increased
     *  simultaneously from here and only when needed.*/
    private val linkedBitSets = IterableArray<UnsafeBitSet>()

    /** Ids that can be reused.*/
    private val emptyIds = IntArrayStackSet()

    /** Indexes of the backing array cells that will be cleared after [removeRequested] call
     * This array will be cleared only after all remove events fired.*/
    private val removeRequests = IntArrayStackSet()

    /** Backing array indexes that subscribers don't know about removing yet.
     * This array is shared with [SubscriptionsManager] which clears its contents ([SubscriptionsManager.freshRemovedEntities]).*/
    private val freshRemovedEntities = freshRemovedEntitiesArray

    /** Indexes of the backing array containing entities not yet known to subscribers.
     * This array is shared with [SubscriptionsManager] which clears its contents ([SubscriptionsManager.freshAddedEntities]).*/
    private val freshAddedEntities = freshAddedEntitiesArray

    @JvmField internal var sendEntityAddedEvents = false

    @JvmField internal var sendEntityRemovedEvents = false



    internal fun linkEntityBitSet(bitSet: UnsafeBitSet) = linkedBitSets.add(bitSet)

    /** Rearrange IDs, removing the gaps. Should not be called while [World.isCurrentlyActing].*/
    internal fun rearrange() {
        if (emptyIds.isEmpty()) return
        var lastElementIndex = nextID - 1
        for (emptyId in emptyIds.backingArray.sortedDescending()) {
            if (lastElementIndex < reservedForSingletons) {
                break
            }
            if (lastElementIndex == emptyId) {
                lastElementIndex--
                continue
            }
            for (componentMapper in componentMappers) {
                componentMapper.replaceId(lastElementIndex, emptyId)
            }
            world.subscriptionsManager.entityChangedId(lastElementIndex, emptyId)
            lastElementIndex--
        }
        nextID = lastElementIndex + 1
        emptyIds.clear()
    }

    /** Creates an Entity with given Components.*/
    fun create(components: Iterable<Component>) {
        val id = create()
        // add components to component mappers
        components.forEach {
            componentMappers[it.getComponentTypeId()].unsafeSet(id, it)
        }
    }

    /** Creates an Entity with given Components.*/
    fun create(components: Array<out Component>) {
        val id = create()
        // add components to component mappers
        components.forEach {
            componentMappers[it.getComponentTypeId()].unsafeSet(id, it)
        }
    }

    /** Creates an entity without components.
     *  @return created Entity id*/
    fun create(): Int {
        val id = if (emptyIds.isNotEmpty()) emptyIds.unsafePoll() else nextID
        if (id == nextID) nextID++ // can poll nextID from empty ids
        size++
        // grow growables if needed
        if (nextID >= componentMappersSize) {
            componentMappersSize *= 2
            componentMappers.forEach { it.grow(componentMappersSize) }
            linkedBitSets.forEach { it.growIfNeeded(componentMappersSize) }
        }
        // add to fresh entities
        freshAddedEntities.add(id)
        // queue event
        if (sendEntityAddedEvents) {
            val event = EntityAddedEvent.pool.obtain()
            event.entity = Entity(id)
            event.entityAsSingleton = null
            world.queueEvent(event)
        }
        world.internalChangeOccurred = true
        return id
    }

    fun addSingletonEntity(singletonEntity: SingletonEntity) {
        val entityID = singletonEntity.entityID
        if (singletons[entityID] != null) return
        singletons[entityID] = singletonEntity
        size++
        // add to fresh entities
        freshAddedEntities.add(entityID)
        // queue event
        if (sendEntityAddedEvents) {
            val event = EntityAddedEvent.pool.obtain()
            event.entity = Entity(entityID)
            event.entityAsSingleton = singletonEntity
            world.queueEvent(event)
        }
        world.internalChangeOccurred = true
    }

    /** Requests to remove the entity.
     *  Due to the fact that after removing an entity, its ID may be taken by another entity, in order to maintain the
     *  constancy of entity IDs throughout the execution of the [World.act] method, the actual removing of all requested
     *  entities occurs at the beginning of the [World.act] method using the [removeRequested] method.*/
    fun requestRemove(id: Int) {
        val requestAdded = removeRequests.add(id)
        if (!requestAdded) return // removing of this entity may already be requested
        freshRemovedEntities.add(id)
        if (sendEntityRemovedEvents) {
            val event = EntityRemovedEvent.pool.obtain()
            event.entity = Entity(id)
            event.entityAsSingleton = if (id >= reservedForSingletons) null else singletons[id]
            world.queueEvent(event)
        }
        world.internalChangeOccurred = true
    }

    /** Performs remove requests that were created by the [requestRemove] method.*/
    fun removeRequested() {
        // remove requests
        removeRequests.forEach { remove(it) }
        removeRequests.clear()
    }

    private fun remove(id: Int) {
        size--
        val isNotSingleton = id >= reservedForSingletons
        if (isNotSingleton) {
            emptyIds.add(id)
            componentMappers.forEach { it.removeComponentSilently(id) }
        }
        else {
            singletons[id] = null
        }
    }

    fun clear() {
        componentMappers.forEach { it.clear() }
        removeRequests.clear()
        freshAddedEntities.clear()
        freshRemovedEntities.clear()
        world.internalChangeOccurred = true
    }

}

