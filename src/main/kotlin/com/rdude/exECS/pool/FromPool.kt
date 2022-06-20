package com.rdude.exECS.pool

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

/** Obtain [Poolable] from default [Pool]). If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized generated calls at compile time */
inline fun <reified T : Poolable> fromPool(): T = fromPool(T::class)

/** Obtain [Poolable] from default [Pool]. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized generated calls at compile time */
fun <T : Poolable> fromPool(kClass: KClass<T>): T = ExEcs.defaultPools[kClass].obtain() as T

/** Obtain [Poolable] from default [Pool] and configure it. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized generated calls at compile time */
inline fun <reified T : Poolable> fromPool(apply: T.() -> Unit): T = fromPool(T::class).apply { apply() }

/** Obtain [Poolable] from default [Pool] and configure it. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized generated calls at compile time */
fun <T : Poolable> fromPool(kClass: KClass<T>, apply: T.() -> Unit): T = (ExEcs.defaultPools[kClass].obtain() as T).apply { apply() }