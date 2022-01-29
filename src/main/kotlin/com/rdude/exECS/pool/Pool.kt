package com.rdude.exECS.pool

import java.util.*

class Pool<T : Poolable>(private val supplier: () -> T) {

    private val queue: Queue<T> by lazy { LinkedList() }

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