package com.rdude.exECS.utils.collections

import kotlin.math.max

internal class IntIterableArray(fixedCapacity: Boolean = false, vararg initialElements: Int) : Iterable<Int> {

    internal var backingArray: IntArray
    internal var size = 0
    private val iterator = ReusableIterator()

    init {
        val length = if (fixedCapacity) initialElements.size else max(16, initialElements.size * 2)
        backingArray = IntArray(length)
        for (i in initialElements.indices) {
            backingArray[i] = initialElements[i]
        }
        size = initialElements.size
    }

    inline fun add(element: Int) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
    }

    inline fun addAll(vararg elements: Int) {
        for (element in elements) {
            add(element)
        }
    }

    inline fun remove(element: Int) {
        for (i in 0 until size) {
            val current = backingArray[i]
            if (current == element) {
                backingArray[i] = backingArray[--size]
                backingArray[size] = 0
            }
        }
    }

    inline fun removeIteratingElement() {
        backingArray[iterator.current] = backingArray[--size]
        backingArray[size] = 0
        iterator.current--
    }

    inline fun clear() {
        backingArray.fill(0, 0, size)
        size = 0
    }

    inline fun isEmpty() = size == 0

    inline fun isNotEmpty() = size > 0

    override fun iterator(): Iterator<Int> {
        iterator.current = 0
        return iterator
    }

    private inline fun grow() {
        backingArray = backingArray.copyOf(max(size, 1) * 2)
    }



    internal inner class ReusableIterator : Iterator<Int> {

        var current = 0

        override fun hasNext(): Boolean = current < size

        override fun next(): Int = backingArray[current++]
    }

}