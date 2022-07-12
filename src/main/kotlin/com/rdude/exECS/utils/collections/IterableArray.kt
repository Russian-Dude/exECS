package com.rdude.exECS.utils.collections

import kotlin.math.max

internal class IterableArray<T> private constructor(array: Array<T?>, initialSize: Int) {

    @JvmField internal var backingArray: Array<T?> = array
    @JvmField internal var size = 0

    init {
        size = initialSize
    }

    inline operator fun get(index: Int) = backingArray[index]

    inline fun add(element: T): Int {
        if (backingArray.size == size) {
            grow()
        }
        val id = size
        backingArray[size++] = element
        return id
    }

    inline fun addAll(vararg elements: T) {
        for (element in elements) {
            add(element)
        }
    }

    inline fun remove(element: T) {
        for (i in 0 until size) {
            val current = backingArray[i]
            if (current == element) {
                backingArray[i] = backingArray[--size]
                backingArray[size] = null
            }
        }
    }

    inline fun removeContainingOrder(element: T) {
        var removed = false
        for (i in 0 until size) {
            val current = backingArray[i]
            if (current == element) {
                removed = true
            }
            if (removed) {
                if (i + 1 < size) {
                    backingArray[i] = backingArray[i + 1]
                }
                else {
                    backingArray[i] = null
                }
            }
        }
        if (removed) size--
    }

    inline fun clear() {
        backingArray.fill(null, 0, size)
        size = 0
    }

    inline fun isEmpty() = size == 0

    inline fun isNotEmpty() = size > 0

    inline fun grow() {
        backingArray = backingArray.copyOf(max(size, 1) * 2)
    }

    inline fun forEach(action: (T) -> Unit) {
        for (i in 0 until size) {
            action.invoke(backingArray[i]!!)
        }
    }

    inline fun removeIf(predicate: (T) -> Boolean) {
        var current = 0
        while (current < size) {
            val isRemove = predicate.invoke(backingArray[current]!!)
            if (isRemove) {
                backingArray[current] = backingArray[--size]
                backingArray[size] = null
            }
            else current++
        }
    }

    inline fun any(predicate: (T) -> Boolean): Boolean {
        for (i in 0 until size) {
            if (predicate.invoke(backingArray[i]!!)) {
                return true
            }
        }
        return false
    }

    inline fun all(predicate: (T) -> Boolean): Boolean {
        for (i in 0 until size) {
            if (!predicate.invoke(backingArray[i]!!)) {
                return false
            }
        }
        return true
    }

    inline fun none(predicate: (T) -> Boolean): Boolean {
        for (i in 0 until size) {
            if (predicate.invoke(backingArray[i]!!)) {
                return false
            }
        }
        return true
    }

    inline fun firstOrNull(predicate: (T) -> Boolean): T? {
        for (i in 0 until size) {
            val t = backingArray[i]!!
            if (predicate.invoke(t)) {
                return t
            }
        }
        return null
    }

    inline fun toList(): List<T> = backingArray.copyOf(size).toList() as List<T>

    infix fun equalsWithAnyOrder(other: IterableArray<T>): Boolean {
        if (this === other) return true
        if (size != other.size) return false
        for (i in 0..backingArray.size - 1) {
            var has = false
            for (j in 0..backingArray.size - 1) {
                if (backingArray[i] == other.backingArray[j]) {
                    has = true
                    continue
                }
            }
            if (!has) return false
        }
        return true
    }

    infix fun notEqualsWithAnyOrder(other: IterableArray<T>) = !equalsWithAnyOrder(other)

    fun snapshot() = IterableArraySnapshot(backingArray, size)

    override fun toString() = backingArray.contentToString()

    /** Used for serialization. */
    class IterableArraySnapshot<T>(val backingArray: Array<T?>, val size: Int)

    companion object {
        internal inline operator fun <reified T> invoke(fixedCapacity: Boolean = false, vararg initialElements: T): IterableArray<T> {
            val length = if (fixedCapacity) initialElements.size else max(16, initialElements.size * 2)
            val array = Array<T?>(length) { null }
            for (i in initialElements.indices) {
                array[i] = initialElements[i]
            }
            return IterableArray(array, initialElements.size)
        }

        operator fun <T> invoke(snapshot: IterableArraySnapshot<T>): IterableArray<T> =
            IterableArray(snapshot.backingArray, snapshot.size)
    }

}