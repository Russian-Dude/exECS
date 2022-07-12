package com.rdude.exECS.exception

import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.Pool
import kotlin.reflect.KClass

/** Throws if [Poolable.returnToPool] method called but [Poolable.pool] property is null.
 * [Poolable] instance was probably created manually instead of being created by the [Pool.obtain] method.*/
class PoolNotSetException internal constructor(type: KClass<out Poolable>): ExEcsException(
    "Can not return Poolable instance of the $type to the Pool. Pool property of the instance is null. " +
            "Poolable instance was probably created manually instead of being created by the Pool.obtain method.")