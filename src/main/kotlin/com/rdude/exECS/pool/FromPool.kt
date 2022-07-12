package com.rdude.exECS.pool

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.exception.DefaultPoolNotExistException
import com.rdude.exECS.exception.DefaultPoolCanNotBeCreatedException
import kotlin.reflect.KClass

/** Obtain [Poolable] of type [T] from the default [Pool]. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized
 * [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Poolables-optimizations) calls at compile time.
 * @throws DefaultPoolNotExistException*/
inline fun <reified T : Poolable> fromPool(): T = fromPool(T::class)

/** Obtain [Poolable] of type [T] from the default [Pool]. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized
 * [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Poolables-optimizations) calls at compile time.
 * @throws DefaultPoolNotExistException*/
fun <T : Poolable> fromPool(kClass: KClass<T>): T = ExEcs.defaultPools[kClass].obtain() as T

/** Obtain [Poolable] of type [T] from the default [Pool] and configure it. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized
 * [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Poolables-optimizations) calls at compile time.
 * @throws DefaultPoolNotExistException*/
inline fun <reified T : Poolable> fromPool(apply: T.() -> Unit): T = fromPool(T::class).apply { apply() }

/** Obtain [Poolable] of type [T] from the default [Pool] and configure it. If exECS compiler plugin is enabled,
 * calls to this method will be replaced with optimized
 * [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Poolables-optimizations) calls at compile time.
 * @throws DefaultPoolNotExistException*/
fun <T : Poolable> fromPool(kClass: KClass<T>, apply: T.() -> Unit): T = (ExEcs.defaultPools[kClass].obtain() as T).apply { apply() }