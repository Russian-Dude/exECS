package com.rdude.exECS.utils.collections

internal class IntArrayStackSet {

    @JvmField internal var backingArray = IntArray(16)

    @JvmField internal val presenceBitSet = BitSet()

    @JvmField internal var size = 0


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

    inline fun forEach(action: (Int) -> Unit) {
        for (i in 0 .. size - 1) {
            action.invoke(backingArray[i])
        }
    }

}