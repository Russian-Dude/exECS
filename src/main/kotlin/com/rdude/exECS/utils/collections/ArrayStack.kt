package com.rdude.exECS.utils.collections

import kotlin.reflect.KClass

internal class ArrayStack<T> private constructor(array: Array<T?>){

    internal var backingArray: Array<T?> = array
    internal var size = 0

    inline fun add(element: T) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
    }

    inline fun poll() : T? = if (size == 0) null else backingArray[--size]

    private inline fun grow() {
        backingArray = backingArray.copyOf(size * 2)
    }

    companion object {
        inline operator fun <reified T> invoke() = ArrayStack<T>(Array(16) { null })
        operator fun <T : Any> invoke(kClass: KClass<T>) = ArrayStack(java.lang.reflect.Array.newInstance(kClass.java, 16) as Array<T?>)
    }

}