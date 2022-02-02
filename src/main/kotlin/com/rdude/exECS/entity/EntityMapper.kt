package com.rdude.exECS.entity

import kotlin.math.max

class EntityMapper {

    private var backingArray = Array<Entity?>(16) { null }
    var size = 1

    init {
        backingArray[EntityID.DUMMY_ENTITY_ID.id] = Entity.DUMMY_ENTITY
    }

    /**
     * Add entity and return index of this entity in backing array which is EntityID
     */
    fun add(entity: Entity) : EntityID {
        if (backingArray.size == size) {
            grow()
        }
        val id = size
        backingArray[size++] = entity
        return EntityID(id)
    }

    fun remove(id: EntityID) {
        backingArray[id.id] = backingArray[--size]
        backingArray[size] = null
    }

    operator fun get(id: EntityID) : Entity = backingArray[id.id] as Entity

    private fun grow() {
        backingArray = backingArray.copyOf(max(size, 1) * 2)
    }
}