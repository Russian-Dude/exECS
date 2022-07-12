package com.rdude.exECS.utils.collections

internal class IntArrayStack : Iterable<Int> {

    @JvmField internal var backingArray = IntArray(16)
    @JvmField internal var size = 0

    private val iterator = ReusableIterator()

    inline fun add(element: Int) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
    }

    inline fun unsafePoll() : Int = backingArray[--size]

    inline fun clear() {
        size = 0
        backingArray.fill(0)
    }

    inline fun isEmpty() = size == 0

    inline fun isNotEmpty() = size > 0

    internal inline fun grow() {
        backingArray = backingArray.copyOf(size * 2)
    }

    override fun iterator(): Iterator<Int> {
        iterator.current = 0
        return iterator
    }

    /** Reusable iterator to reduce garbage collector calls */
    internal inner class ReusableIterator : IntIterator() {

        var current = 0

        override fun hasNext(): Boolean = current < size

        override fun nextInt(): Int = backingArray[current++]
    }

}