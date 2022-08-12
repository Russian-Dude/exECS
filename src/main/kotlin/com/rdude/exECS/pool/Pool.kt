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
        t.isInPool = false
        return t
    }

    fun add(t: T)  {
        t.reset()
        if (t.isInPool) {
            throw IllegalStateException("Can not add Poolable $this to the Pool. It is already in the Pool.")
        }
        t.isInPool = true
        queue.add(t)
    }

    companion object {
        inline operator fun <reified T : Poolable> invoke(noinline supplier: () -> T) = Pool(supplier, T::class)
    }

}