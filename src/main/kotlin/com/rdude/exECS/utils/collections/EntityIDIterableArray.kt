package com.rdude.exECS.utils.collections

import com.rdude.exECS.entity.EntityID

internal class EntityIDIterableArray(fixedCapacity: Boolean = false, vararg initialElements: Int) : Iterable<EntityID> {

    private var backingArray: IntArray
    private var size = 0
    private val iterator = ReusableIterator()

    init {
        val length = if (fixedCapacity) initialElements.size else kotlin.math.max(16, initialElements.size * 2)
        backingArray = IntArray(length)
        for (i in initialElements.indices) {
            backingArray[i] = initialElements[i]
        }
        size = initialElements.size
    }

    fun add(id: EntityID) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = id.id
    }

    fun remove(id: EntityID) {
        for (i in 0 until size) {
            val current = backingArray[i]
            if (current == id.id) {
                backingArray[i] = backingArray[--size]
                backingArray[size] = 0
            }
        }
    }

    fun contains(id: EntityID) : Boolean {
        for (i in 0 until backingArray.size) {
            if (backingArray[i] == id.id) {
                return true
            }
        }
        return false
    }

    fun clear() {
        backingArray.fill(0, 0, size)
        size = 0
    }

    fun isEmpty() = size == 0

    fun isNotEmpty() = size > 0

    override fun iterator(): Iterator<EntityID> {
        iterator.current = 0
        return iterator
    }

    private fun grow() {
        backingArray = backingArray.copyOf(kotlin.math.max(size, 1) * 2)
    }



    private inner class ReusableIterator : Iterator<EntityID> {

        var current = 0

        override fun hasNext(): Boolean = current < size

        override fun next(): EntityID = EntityID(backingArray[current++])
    }

}