package com.rdude.exECS.pool

interface Poolable {

    var pool: Pool<Poolable>

    fun returnToPool() = pool.retrieve(this)

}