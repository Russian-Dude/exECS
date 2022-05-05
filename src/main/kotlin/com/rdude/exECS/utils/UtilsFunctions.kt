package com.rdude.exECS.utils

import kotlin.math.max

/** Decrease value of given key by 1 and return result. Value can not be less than 0.
 *  If there is no mapping, set value to 0. */
internal inline fun <T> MutableMap<T, Int>.decreaseCount(key: T): Int {
    val result = this[key]?.let { max(it - 1, 0) } ?: 0
    this[key] = result
    return result
}

/** Increase value of given key by 1 and return result. If there is no mapping, set value to 1. */
internal inline fun <T> MutableMap<T, Int>.increaseCount(key: T): Int {
    val result = this[key]?.let { it + 1 } ?: 1
    this[key] = result
    return result
}