package com.rdude.exECS.config

import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.exception.AlreadyInPoolException

/** Describes what happens when [Poolable.returnToPool] is called but [Poolable] is already in [Pool] ([Poolable.isInPool] is true).*/
enum class OnAlreadyInPool {

    /** [AlreadyInPoolException] will be thrown.*/
    THROW,

    /** Nothing will happen.*/
    IGNORE

}