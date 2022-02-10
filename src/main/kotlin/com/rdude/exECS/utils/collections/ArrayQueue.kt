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

    private fun rearrangeAndGrow() {
        val newBackingArray = backingArray.copyOf(size * 2)
        for (i in 0..size - 1) {
            newBackingArray[i] = backingArray[(head + 1) % backingArray.size]
        }
        backingArray = newBackingArray
        head = 0
        tail = size
    }

    companion object {
        internal inline fun <reified T> create() = ArrayQueue<T>(Array(16) { null })
    }


}