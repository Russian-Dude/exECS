package com.rdude.exECS.utils.collections

internal class IntArrayStackSet : Iterable<Int> {

    internal var backingArray = IntArray(16)

    internal val presenceBitSet = BitSet()

    private val iterator = ReusableIterator()

    internal var size = 0


    inline fun add(element: Int): Boolean {
        if (presenceBitSet[element]) return false
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
        presenceBitSet.set(element)
        return true
    }

    inline fun unsafePoll(): Int  {
        val result = backingArray[--size]
        presenceBitSet.clear(result)
        return result
    }

    inline fun clear() {
        size = 0
        backingArray.fill(0)
        presenceBitSet.clear()
    }

    inline fun isEmpty() = size == 0

    inline fun isNotEmpty() = size > 0

    internal inline fun grow() {
        val newSize = size * 2
        backingArray = backingArray.copyOf(newSize)
    }

    override fun iterator(): Iterator<Int> {
        iterator.current = 0
        return iterator
    }

    /** Reusable iterator to reduce garbage collector calls */
    internal inner class ReusableIterator : Iterator<Int> {

        var current = 0

        override fun hasNext(): Boolean = current < size

        override fun next(): Int = backingArray[current++]
    }

}