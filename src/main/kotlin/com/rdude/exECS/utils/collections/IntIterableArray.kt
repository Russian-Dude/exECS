package com.rdude.exECS.utils.collections

import kotlin.math.max

class IntIterableArray(fixedCapacity: Boolean = false, vararg initialElements: Int) {

    @JvmField var backingArray: IntArray
    @JvmField var size = 0

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

    internal inline fun addAll(vararg elements: Int) {
        for (element in elements) {
            add(element)
        }
    }

    internal inline fun remove(element: Int) {
        for (i in 0 until size) {
            val current = backingArray[i]
            if (current == element) {
                backingArray[i] = backingArray[--size]
                backingArray[size] = 0
            }
        }
    }

    inline fun clear() {
        backingArray.fill(0, 0, size)
        size = 0
    }

    inline fun isEmpty() = size == 0

    inline fun isNotEmpty() = size > 0

    inline fun grow() {
        backingArray = backingArray.copyOf(max(size, 1) * 2)
    }

    inline fun forEach(action: (Int) -> Unit) {
        for (i in 0 until size) {
            action.invoke(backingArray[i])
        }
    }

    internal inline fun removeIf(predicate: (Int) -> Boolean) {
        var current = 0
        while (current < size) {
            val isRemove = predicate.invoke(backingArray[current])
            if (isRemove) {
                backingArray[current] = backingArray[--size]
                backingArray[size] = 0
            }
            else current++
        }
    }

    inline fun any(predicate: (Int) -> Boolean): Boolean {
        for (i in 0 until size) {
            if (predicate.invoke(backingArray[i])) {
                return true
            }
        }
        return false
    }

    inline fun all(predicate: (Int) -> Boolean): Boolean {
        for (i in 0 until size) {
            if (!predicate.invoke(backingArray[i])) {
                return false
            }
        }
        return true
    }

    inline fun none(predicate: (Int) -> Boolean): Boolean {
        for (i in 0 until size) {
            if (predicate.invoke(backingArray[i])) {
                return false
            }
        }
        return true
    }

    override fun toString(): String {
        return backingArray.contentToString()
    }

}