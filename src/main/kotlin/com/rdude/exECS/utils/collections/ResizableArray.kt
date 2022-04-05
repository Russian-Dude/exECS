package com.rdude.exECS.utils.collections

import kotlin.reflect.KClass

class ResizableArray<T> private constructor(private var backingArray: Array<T?>) {

    operator fun set(index: Int, value: T?) {
        growIfNeeded(index)
        backingArray[index] = value
    }

    operator fun get(index: Int): T? = if (backingArray.size <= index) null else backingArray[index]

    fun getUnsafe(index: Int): T? = backingArray[index]

    private inline fun growIfNeeded(toFitId: Int) {
        if (backingArray.size <= toFitId) {
            backingArray = backingArray.copyOf(backingArray.size * 2)
        }
    }

    companion object {
        inline operator fun <reified T: Any> invoke() = Companion.invoke(T::class)
        operator fun <T : Any> invoke(kClass: KClass<T>) = ResizableArray(java.lang.reflect.Array.newInstance(kClass.java, 16) as Array<T?>)
    }

}