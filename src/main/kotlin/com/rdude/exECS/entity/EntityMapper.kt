package com.rdude.exECS.entity

import kotlin.math.max

class EntityMapper {

    private var backingArray = Array<Entity?>(16) { null }
    private var backingArrayActualSize = 1
    private var emptyIDs = IntArray(16)
    private var emptyIDsAmount = 0

    init {
        backingArray[EntityID.DUMMY_ENTITY_ID.id] = Entity.DUMMY_ENTITY
    }

    /**
     * Add entity and return index of this entity in backing array which is EntityID
     */
    fun add(entity: Entity) : EntityID {

        // if any id is empty
        if (emptyIDsAmount > 0) {
            backingArray[--emptyIDsAmount] = entity
            return EntityID(emptyIDsAmount)
        }

        // if there are no empty ids
        if (backingArray.size == backingArrayActualSize) {
            growBackingArray()
        }
        val id = backingArrayActualSize
        backingArray[backingArrayActualSize++] = entity
        return EntityID(id)
    }

    fun remove(id: EntityID) {
        backingArray[id.id] = null
        if (emptyIDs.size == emptyIDsAmount) {
            growEmptyIDsArray()
        }
        emptyIDs[emptyIDsAmount++] = id.id
    }

    operator fun get(id: EntityID) : Entity = backingArray[id.id] as Entity

    private fun growBackingArray() {
        backingArray = backingArray.copyOf(max(backingArrayActualSize, 1) * 2)
    }

    private fun growEmptyIDsArray() {
        emptyIDs = emptyIDs.copyOf(max(emptyIDsAmount, 1) * 2)
    }
}