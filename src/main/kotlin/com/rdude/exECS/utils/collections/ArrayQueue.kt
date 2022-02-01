package com.rdude.exECS.utils.collections

class ArrayQueue<T> {

    private var backingArray: Array<T?> = java.lang.reflect.Array.newInstance(Any::class.java, 16) as Array<T?>
    private var size = 0

    fun add(element: T) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
    }

    fun poll() : T? = if (size == 0) null else backingArray[--size]

    private fun grow() {
        backingArray = backingArray.copyOf(size * 2)
    }

}