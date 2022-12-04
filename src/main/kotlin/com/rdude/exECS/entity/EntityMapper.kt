package com.rdude.exECS.entity

import com.rdude.exECS.aspect.SubscriptionsManager
import com.rdude.exECS.component.ChildEntityComponent
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.component.ParentEntityComponent
import com.rdude.exECS.event.ChildEntityAddedEvent
import com.rdude.exECS.event.ChildEntityRemovedEvent
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.exception.NoEntityException
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.IntArrayStackSet
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.utils.fastForEachIndexed
import com.rdude.exECS.world.World

internal class EntityMapper(
    private var world: World,
    freshAddedEntitiesArray: IntIterableArray,
    freshRemovedEntitiesArray: IntArrayStackSet,
    initialCapacity: Int
) {

    /** Current backing array size of component mappers
     * Stored here to calculate new size only once and only when entity with id that exceeds current size is added.*/
    @JvmField internal var componentMappersSize: Int = initialCapacity

    /** Amount of IDs reserved for singletons.*/
    @JvmField internal val reservedForSingletons = ExEcs.singletonEntityIDsResolver.size

    /** Stores component mappers for every component type. Array index - component type id.*/
    @JvmField internal val componentMappers: Array<ComponentMapper<*>> =
        Array(ExEcs.componentTypeIDsResolver.size) { ComponentMapper(it, world, componentMappersSize) }

    /** Faster access to [ParentEntityComponent] mapper.*/
    @JvmField
    @Suppress("UNCHECKED_CAST")
    internal val parentEntityComponents: ComponentMapper<ParentEntityComponent> =
        componentMappers[ParentEntityComponent::class.componentTypeId] as ComponentMapper<ParentEntityComponent>

    /** Faster access to [ChildEntityComponent] mapper.*/
    @JvmField
    @Suppress("UNCHECKED_CAST")
    internal val childEntityComponents: ComponentMapper<ChildEntityComponent> =
        componentMappers[ChildEntityComponent::class.componentTypeId] as ComponentMapper<ChildEntityComponent>

    /** Singleton instances.*/
    @JvmField internal val singletons: Array<SingletonEntity?> = Array(reservedForSingletons) { null }

    /** Next free ID. Initially: first ID not reserved for singletons.*/
    @JvmField internal var nextID: Int = reservedForSingletons

    /** Current entities amount.*/
    @JvmField internal var size = 0

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

    @JvmField internal var sendChildEntityAddedEvents = false

    @JvmField internal var sendChildEntityRemovedEvents = false



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

    /** Creates an Entity with given Components and returns it.*/
    fun create(components: Iterable<Component>): Entity {
        val id = create()
        // add components to component mappers
        components.forEach {
            componentMappers[it.getComponentTypeId()].addComponentUnsafe(id, it, world.configuration.queueComponentAddedWhenEntityAdded)
        }
        return Entity(id)
    }

    /** Creates an Entity with given Components and returns it.*/
    fun create(components: Array<out Component>): Entity {
        val id = create()
        // add components to component mappers
        components.forEach {
            componentMappers[it.getComponentTypeId()].addComponentUnsafe(id, it, world.configuration.queueComponentAddedWhenEntityAdded)
        }
        return Entity(id)
    }

    /** Creates an [Entity] without components.
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
            world.queueEvent(event)
        }
        world.internalChangeOccurred = true
        return id
    }

    fun addSingletonEntity(singletonEntity: SingletonEntity) {
        val entityID = singletonEntity.entityID
        if (singletons[entityID] != null) return
        singletons[entityID] = singletonEntity
        singletonEntity.componentsCache.fastForEachIndexed { index, component ->
            if (component != null) {
                componentMappers[index].addComponentUnsafe(entityID, component, world.configuration.queueComponentAddedWhenEntityAdded)
            }
            else {
                componentMappers[index].removeComponent(entityID, false)
            }
        }
        size++
        // add to fresh entities
        freshAddedEntities.add(entityID)
        // queue event
        if (sendEntityAddedEvents) {
            val event = EntityAddedEvent.pool.obtain()
            event.entity = Entity(entityID)
            world.queueEvent(event)
        }
        // update generated fields based on the current world
        ExEcs.generatedFieldsInitializer.singletonEntityAdded(singletonEntity, world)
        world.internalChangeOccurred = true
    }

    /** Requests to remove the entity.
     *  Due to the fact that after removing an entity, its ID may be taken by another entity, in order to maintain the
     *  constancy of entity IDs throughout the execution of the [World.act] method, the actual removing of all requested
     *  entities occurs at the beginning of the [World.act] method using the [removeRequested] method.*/
    fun requestRemove(id: Int) {
        if (id < 0) throw NoEntityException("Can not remove Entity.NO_ENTITY")
        val requestAdded = removeRequests.add(id)
        if (!requestAdded) return // removing of this entity may already be requested
        freshRemovedEntities.add(id)
        // events
        if (sendEntityRemovedEvents) {
            val event = EntityRemovedEvent.pool.obtain()
            event.entity = Entity(id)
            world.queueEvent(event)
        }

        // remove from parent
        val childComponent = childEntityComponents[id]
        if (childComponent != null) {
            removeChildEntity(childComponent.parentEntityId, id)
        }
        // request remove children
        parentEntityComponents[id]?.children?.forEach { requestRemove(it.id) }

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
        val parentEntityComponent = parentEntityComponents[id]
        if (parentEntityComponent != null) {
            parentEntityComponent.children.forEach { child ->
                childEntityComponents.removeComponent(child.id, false)
            }
            parentEntityComponents.removeComponent(id, false)
        }
        val isNotSingleton = id >= reservedForSingletons
        if (isNotSingleton) {
            emptyIds.add(id)
            val eventAllowed = world.configuration.queueComponentRemovedWhenEntityRemoved
            componentMappers.forEach { it.removeComponent(id, eventAllowed) }
        }
        else {
            singletons[id]?.world = null
            // update generated fields based on the current world
            ExEcs.generatedFieldsInitializer.singletonEntityRemoved(singletons[id]!!, world)
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

    fun addChildEntity(parent: Int, child: Int) {
        if (parent == child) throw IllegalArgumentException("Entity cannot be a child of itself")
        if (parent < 0) throw NoEntityException("Can not add a child Entity. Parent Entity is Entity.NO_ENTITY")
        if (child < 0) throw NoEntityException("Can not add a child Entity. Child Entity is Entity.NO_ENTITY")
        val parentComponent = parentEntityComponents[parent]
            ?: ParentEntityComponent.pool.obtain().apply { parentEntityComponents.addComponent(parent, this, false) }
        val childComponent = childEntityComponents[child]
            ?: ChildEntityComponent.pool.obtain().apply { childEntityComponents.addComponent(child, this, false) }
        parentComponent.children.add(child)
        if (childComponent.parent != Entity.NO_ENTITY) {
            parentEntityComponents[childComponent.parentEntityId]!!.children.remove(child)
        }
        childComponent.parentEntityId = parent

        if (sendChildEntityAddedEvents) {
            val event = ChildEntityAddedEvent.pool.obtain()
            event.childEntity = Entity(child)
            event.parentEntity = Entity(parent)
            world.queueEvent(event)
        }
    }

    fun removeChildEntity(parent: Int, child: Int) {
        if (parent < 0) throw NoEntityException("Can not remove child Entity. Parent Entity is Entity.NO_ENTITY")
        if (child < 0) throw NoEntityException("Can not remove child Entity. Child Entity is Entity.NO_ENTITY")
        childEntityComponents.removeComponent(child, false)
        val parentComponent = parentEntityComponents[parent]!!
        val children = parentComponent.children
        children.remove(child)
        if (children.size == 0) parentEntityComponents.removeComponent(parent, false)

        if (sendChildEntityRemovedEvents) {
            val event = ChildEntityRemovedEvent.pool.obtain()
            event.childEntity = Entity(child)
            event.parentEntity = Entity(parent)
            world.queueEvent(event)
        }
    }

    fun growSizeTo(newSize: Int) {
        if (componentMappersSize >= newSize) return
        componentMappersSize = newSize
        componentMappers.forEach { it.grow(componentMappersSize) }
        linkedBitSets.forEach { it.growIfNeeded(componentMappersSize) }
    }

}

