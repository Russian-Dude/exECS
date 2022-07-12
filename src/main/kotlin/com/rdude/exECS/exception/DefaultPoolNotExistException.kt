package com.rdude.exECS.exception

import com.rdude.exECS.pool.Poolable
import kotlin.reflect.KClass

class DefaultPoolNotExistException internal constructor(type: KClass<out Poolable>) : ExEcsException(
    "Can not obtain Poolable instance of $type from the default Pool. Default Pool for this type does not exist")