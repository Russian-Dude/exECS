package com.rdude.exECS.config

import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.exception.PoolNotSetException
import com.rdude.exECS.exception.DefaultPoolNotExistException

/** Describes what happens when [Poolable.returnToPool] is called and [Poolable.pool] is null.*/
enum class OnPoolIsNotSet {

    /** [PoolNotSetException] will be thrown.*/
    THROW,

    /** Nothing will happen.*/
    IGNORE,

    /** [Poolable] will be returned to the default Pool.
     * If the default Pool for this Poolable type does not exist [DefaultPoolNotExistException] will be thrown.*/
    RETURN_TO_DEFAULT_POOL

}