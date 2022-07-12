package com.rdude.exECS.utils.collections

import kotlin.math.max

/**
 * Every element in this array keeps its ID as long as it presents in the backing array.
 * Empty cells will be reused.
 * When this array size grows it also ask all linked bitsets to grow their size if needed
 */
internal class IdArray<T> private constructor(array: Array<T?>) {

    private var backingArray: Array<T?> = array
    private val emptyIDs = IntArrayStack()
    private val linkedBitSets = IterableArray<UnsafeBitSet>()
    internal var size = 0
        private set
    private var lastIndex = 0

    inline operator fun get(index: Int) = backingArray[index]

    inline operator fun set(index: Int, element: T?) {
        while (backingArray.size <= index) {
            grow()
        }
        val oldElement = backingArray[index]
        if (oldElement != null && element == null) {
            size--
            emptyIDs.add(index)
        }
        else if (oldElement == null && element != null) {
            size++
            if (lastIndex == index) {
                lastIndex++
                while (backingArray[lastIndex] != null) {
                    if (backingArray.size <= lastIndex) {
                        grow()
                    }
                    lastIndex++
                }
            }
        }
        backingArray[index] = element
    }

    inline fun add(element: T): Int {
        if (backingArray.size == size) {
            grow()
        }
        if (emptyIDs.isNotEmpty()) {
            var id = emptyIDs.unsafePoll()
            var emptied = false
            while (backingArray[id] != null) {
                if (emptyIDs.isNotEmpty()) {
                    id = emptyIDs.unsafePoll()
                }
                else {
                    emptied = true
                    break
                }
            }
            if (!emptied) {
                backingArray[id] = element
                size++
                return id
            }
        }
        val id = lastIndex
        backingArray[lastIndex++] = element
        size++
        return id
    }

    inline fun addAll(vararg elements: T) {
        for (element in elements) {
            add(element)
        }
    }

    inline fun clear() {
        backingArray.fill(null, 0, size)
        emptyIDs.clear()
        size = 0
        lastIndex = 0
    }

    inline fun isEmpty() = size == 0

    inline fun isNotEmpty() = size > 0

    inline fun linkBitSet(bitSet: UnsafeBitSet) = linkedBitSets.add(bitSet)

    inline fun grow() {
        val newSize = max(backingArray.size, 1) * 2
        backingArray = backingArray.copyOf(newSize)
        linkedBitSets.forEach { it.growIfNeeded(newSize) }
    }

    companion object {
        internal inline operator fun <reified T> invoke(): IdArray<T> = IdArray(Array(16) { null })
    }

}