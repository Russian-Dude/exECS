package com.rdude.exECS.utils.collections

internal class ArrayQueue<T> private constructor(array: Array<T?>){

    private var backingArray: Array<T?> = array
    private var head = 0
    private var tail = 0
    private var size = 0

    fun add(element: T) {
        if (size == backingArray.size) {
            rearrangeAndGrow()
        }
        backingArray[tail++] = element
        if (tail == backingArray.size) {
            tail = 0
        }
        size++
    }

    fun poll(): T? {
        val result = backingArray[head]
        if (result != null) {
            backingArray[head] = null
            size--
            head++
            if (head == backingArray.size) {
                head = 0
            }
        }
        return result
    }

    fun asArray(): Array<T?> {
        val result = backingArray.copyOf()
        for (i in 0..size - 1) {
            result[i] = backingArray[(head + i) % backingArray.size]
        }
        return result
    }

    internal fun setBackingArrayUnsafe(newArray: Array<*>, size: Int = newArray.count { it != null }) {
        this.backingArray = newArray as Array<T?>
        this.head = 0
        this.tail = 0
        this.size = size
    }

    private fun rearrangeAndGrow() {
        val newBackingArray = backingArray.copyOf(size * 2)
        for (i in 0..size - 1) {
            newBackingArray[i] = backingArray[(head + i) % backingArray.size]
        }
        backingArray = newBackingArray
        head = 0
        tail = size
    }

    companion object {
        internal inline operator fun <reified T> invoke() = ArrayQueue<T>(Array(16) { null })
    }


}