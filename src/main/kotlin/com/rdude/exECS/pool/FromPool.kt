package com.rdude.exECS.pool

import kotlin.reflect.KClass

/** Obtain [Poolable] from pool. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized generated calls at compile time */
inline fun <reified T : Poolable> fromPool() : T = fromPool(T::class)

/** Obtain [Poolable] from pool. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized generated calls at compile time */
fun <T : Poolable> fromPool(kClass: KClass<T>) : T = Poolable.defaultPool(kClass).obtain() as T