package com.rdude.exECS.utils.collections

class LongIterableArray(fixedCapacity: Boolean = false, vararg initialElements: Long) : Iterable<Long> {

    private var backingArray: LongArray
    private var size = 0
    private val iterator = ReusableIterator()

    init {
        val length = if (fixedCapacity) initialElements.size else kotlin.math.max(16, initialElements.size * 2)
        backingArray = LongArray(length)
        for (i in initialElements.indices) {
            backingArray[i] = initialElements[i]
        }
        size = initialElements.size
    }

    fun add(element: Long) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
    }

    fun addAll(vararg elements: Long) {
        for (element in elements) {
            add(element)
        }
    }

    fun remove(element: Long) {
        for (i in 0 until size) {
            val current = backingArray[i]
            if (current == element) {
                backingArray[i] = backingArray[--size]
                backingArray[size] = 0
            }
        }
    }

    fun clear() {
        backingArray.fill(0L, 0, size)
        size = 0
    }

    fun isEmpty() = size == 0

    fun isNotEmpty() = size > 0

    override fun iterator(): LongIterator {
        iterator.current = 0
        return iterator
    }

    private fun grow() {
        backingArray = backingArray.copyOf(kotlin.math.max(size, 1) * 2)
    }



    private inner class ReusableIterator : LongIterator() {

        var current = 0

        override fun hasNext(): Boolean = current < size

        override fun nextLong(): Long = backingArray[current++]
    }

}