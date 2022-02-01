package com.rdude.exECS.pool

import com.rdude.exECS.utils.collections.ArrayQueue

class Pool<T : Poolable>(private val supplier: () -> T) {

    private val queue: ArrayQueue<T> by lazy { ArrayQueue() }

    fun obtain() : T {
        var t = queue.poll()
        if (t == null) {
            t = supplier.invoke()
            t.pool = this as Pool<Poolable>
        }
        return t
    }

    fun retrieve(t: T) = queue.add(t)

}