package com.rdude.exECS.exception

import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.config.OnAlreadyInPool
import com.rdude.exECS.pool.Poolable
import kotlin.reflect.KClass

/** Throws if [Poolable.returnToPool] method called but [Poolable.isInPool] property is true and
 * [ExEcsGlobalConfiguration.onAlreadyInPool] is set to [OnAlreadyInPool.THROW].*/
class AlreadyInPoolException internal constructor(type: KClass<out Poolable>): ExEcsException(
    "Can not return Poolable instance of the $type to the Pool. This Poolable is already in a Pool. " +
            "This exception was thrown because ExEcsGlobalConfiguration.onAlreadyInPool property is set to OnAlreadyInPool.THROW."
)