package com.rdude.exECS.pool

import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.config.OnAlreadyInPool
import com.rdude.exECS.exception.AlreadyInPoolException
import com.rdude.exECS.utils.collections.ArrayStack
import kotlin.reflect.KClass

class Pool<T : Poolable> @PublishedApi internal constructor(private val supplier: () -> T, kClass: KClass<T>) {

    private val stack: ArrayStack<T> = ArrayStack(kClass)

    fun obtain() : T {
        var t = stack.poll()
        if (t == null) {
            t = supplier.invoke()
            t.pool = this as Pool<Poolable>
        }
        t.isInPool = false
        return t
    }

    fun add(t: T) {
        if (t.isInPool) {
            if (ExEcsGlobalConfiguration.onAlreadyInPool == OnAlreadyInPool.THROW)
                throw AlreadyInPoolException(t::class)
        }
        else {
            t.reset()
            t.isInPool = true
            stack.add(t)
        }
    }

    companion object {
        inline operator fun <reified T : Poolable> invoke(noinline supplier: () -> T) = Pool(supplier, T::class)
    }

}