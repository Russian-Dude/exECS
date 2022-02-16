package com.rdude.exECS.event

import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

abstract class InternalPoolableEvent : InternalEvent(), Poolable {

    override lateinit var pool: Pool<Poolable>

}