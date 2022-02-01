package com.rdude.exECS.utils.collections

import kotlin.math.max

class IterableArray<T>(fixedCapacity: Boolean = false, vararg initialElements: T) : Iterable<T> {

    private var backingArray: Array<T?>
    private var size = 0
    private val iterator = ReusableIterator()

    init {
        val length = if (fixedCapacity) initialElements.size else max(16, initialElements.size * 2)
        backingArray = java.lang.reflect.Array.newInstance(Any::class.java, length) as Array<T?>
        for (i in initialElements.indices) {
            backingArray[i] = initialElements[i]
        }
        size = initialElements.size
    }

    fun add(element: T) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
    }

    fun addAll(vararg elements: T) {
        for (element in elements) {
            add(element)
        }
    }

    fun remove(element: T) {
        for (i in 0 until size) {
            val current = backingArray[i]
            if (current == element) {
                backingArray[i] = backingArray[--size]
                backingArray[size] = null
            }
        }
    }

    fun clear() {
        backingArray.fill(null, 0, size)
        size = 0
    }

    fun isEmpty() = size == 0

    fun isNotEmpty() = size > 0

    override fun iterator(): Iterator<T> {
        iterator.current = 0
        return iterator
    }

    private fun grow() {
        backingArray = backingArray.copyOf(max(size, 1) * 2)
    }



    private inner class ReusableIterator : Iterator<T> {

        var current = 0

        override fun hasNext(): Boolean = current < size

        override fun next(): T = backingArray[current++] as T
    }

}