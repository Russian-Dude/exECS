package com.rdude.exECS.exception

import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.config.OnPoolIsNotSet
import com.rdude.exECS.pool.Poolable
import kotlin.reflect.KClass

/** Throws if [Poolable.returnToPool] method called but [Poolable.pool] property is null and
 * [ExEcsGlobalConfiguration.onPoolIsNotSet] is set to [OnPoolIsNotSet.THROW].*/
class PoolNotSetException internal constructor(type: KClass<out Poolable>) : ExEcsException(
    "Can not return Poolable instance of the $type to the Pool. Pool property of the instance is null. " +
            "Poolable instance was probably created manually instead of being created by the Pool.obtain method. " +
            "This exception was thrown because ExEcsGlobalConfiguration.onPoolIsNotSet property is set to OnPoolIsNotSet.THROW."
)