package com.rdude.exECS.utils.collections

internal class IntArrayStack {

    internal var backingArray = IntArray(16)
    internal var size = 0

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

}