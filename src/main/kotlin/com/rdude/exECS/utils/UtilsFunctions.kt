package com.rdude.exECS.utils

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.Event
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.collections.ComponentTypeToEntityPair
import com.rdude.exECS.utils.collections.EventTypeToComponentTypePair
import com.rdude.exECS.system.System
import kotlin.math.max
import kotlin.reflect.KClass

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

@PublishedApi
internal val KClass<out Component>.componentTypeId get() = ExEcs.componentTypeIDsResolver.idFor(this)

@PublishedApi
internal val KClass<out Event>.eventTypeId get() = ExEcs.eventTypeIDsResolver.idFor(this)

@PublishedApi
internal val KClass<out SingletonEntity>.singletonTypeId get() = ExEcs.singletonEntityIDsResolver.idFor(this)

@PublishedApi
internal val KClass<out System>.systemTypeId get() = ExEcs.systemTypeIDsResolver.idFor(this)

internal val EventSystem<*>.eventsTypesSubscription get() = ExEcs.eventSystemGenericQualifier.getEventsTypesSubscriptionForSystem(this)

internal inline fun IntArray.changeEach(apply: (Int) -> Int) {
    for (i in 0..size - 1) {
        this[i] = apply(this[i])
    }
}

internal inline fun LongArray.changeEach(apply: (Long) -> Long) {
    for (i in 0..size - 1) {
        this[i] = apply(this[i])
    }
}

internal inline fun IntArray.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

internal inline fun LongArray.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

internal inline fun <T : Any?> Array<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1]
    this[index1] = this[index2]
    this[index2] = tmp
}

/** For each loop without iterator instantiation.*/
internal inline fun IntArray.fastForEach(action: (Int) -> Unit) {
    for (i in 0..size - 1) {
        action(get(i))
    }
}

/** For each loop without iterator instantiation.*/
internal inline fun LongArray.fastForEach(action: (Long) -> Unit) {
    for (i in 0..size - 1) {
        action(get(i))
    }
}

/** For each loop without iterator instantiation.*/
internal inline fun <T> Array<T>.fastForEach(action: (T) -> Unit) {
    for (i in 0..size - 1) {
        action(get(i))
    }
}

/** For each loop without iterator instantiation.*/
internal inline fun <T> Array<T>.fastForEachIndexed(action: (index: Int, T) -> Unit) {
    for (i in 0..size - 1) {
        action(i, get(i))
    }
}

internal inline fun <T> Array<T>.getOrPut(index: Int, put: T): T = get(index) ?: put.apply { set(index, put) }

/** Wraps long value to [ComponentTypeToEntityPair] value class.*/
internal inline fun Long.asComponentToEntity() = ComponentTypeToEntityPair(this)

/** Wraps long value to [EventTypeToComponentTypePair] value class.*/
internal inline fun Long.asEventToComponent() = EventTypeToComponentTypePair(this)

internal inline fun Long.getFirstInt(): Int = (this shr 32).toInt()

internal inline fun Long.withChangedFirstInt(value: Int): Long = (value.toLong() shl 32) + getSecondInt()

internal inline fun Long.getSecondInt(): Int = this.toInt()

internal inline fun Long.withChangedSecondInt(value: Int): Long = this - this.toInt() + value

internal inline fun longFromInts(a: Int, b: Int): Long = (a.toLong() shl 32) + b