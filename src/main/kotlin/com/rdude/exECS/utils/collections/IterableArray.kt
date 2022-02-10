package com.rdude.exECS.utils.collections

import kotlin.math.max

internal class IterableArray<T>
private constructor(array: Array<T?>, initialSize: Int) : Iterable<T> {

    internal var backingArray: Array<T?> = array
    internal var size = 0
    private val iterator = ReusableIterator()

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

    inline fun removeIteratingElement() {
        backingArray[iterator.current] = backingArray[--size]
        backingArray[size] = null
        iterator.current--
    }

    inline fun clear() {
        backingArray.fill(null, 0, size)
        size = 0
    }

    inline fun isEmpty() = size == 0

    inline fun isNotEmpty() = size > 0

    override fun iterator(): Iterator<T> {
        iterator.current = 0
        return iterator
    }

    inline fun grow() {
        backingArray = backingArray.copyOf(max(size, 1) * 2)
    }

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


    internal inner class ReusableIterator : Iterator<T> {

        var current = 0

        override fun hasNext(): Boolean = current < size

        override fun next(): T = backingArray[current++] as T
    }

    companion object {
        internal inline operator fun <reified T> invoke(fixedCapacity: Boolean = false, vararg initialElements: T): IterableArray<T> {
            val length = if (fixedCapacity) initialElements.size else max(16, initialElements.size * 2)
            val array = Array<T?>(length) { null }
            for (i in initialElements.indices) {
                array[i] = initialElements[i]
            }
            return IterableArray(array, initialElements.size)
        }
    }

}