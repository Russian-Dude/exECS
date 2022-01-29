package com.rdude.exECS.event

import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/**
 * Poolable event will be automatically returned to pool after processing by event bus
 */
open class PoolableEvent : Event, Poolable {

    override lateinit var pool: Pool<Poolable>

    fun returnToPool() = pool.retrieve(this)

}