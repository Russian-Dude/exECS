package com.rdude.exECS.pool

import com.rdude.exECS.utils.collections.ArrayStack
import kotlin.reflect.KClass

class Pool<T : Poolable>(private val supplier: () -> T, kClass: KClass<T>) {

    private val queue: ArrayStack<T> by lazy { ArrayStack(kClass) }

    fun obtain() : T {
        var t = queue.poll()
        if (t == null) {
            t = supplier.invoke()
            t.pool = this as Pool<Poolable>
        }
        return t
    }

    fun retrieve(t: T) = queue.add(t)

    companion object {
        inline operator fun <reified T : Poolable> invoke(noinline supplier: () -> T) = Pool(supplier, T::class)
    }

}