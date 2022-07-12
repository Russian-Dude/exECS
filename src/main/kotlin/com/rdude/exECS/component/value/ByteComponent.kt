package com.rdude.exECS.component.value

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.observable.ObservableByteComponent

/** Component representing some [Byte] value. Allows to perform some actions directly on the component without the need
 *  to access the value.
 * ```
 * myComponent += 717 // math operators with primitives
 * myComponent /= myAnotherComponent // math operators with another primitive components
 * maxOf(myComponent, myAnotherComponent) // comparable by value
 * 0..myComponent // creating ranges
 * ```
 * @see [ObservableByteComponent]*/
abstract class ByteComponent(open var value: Byte = 0) : Component, Comparable<ByteComponent> {

    operator fun compareTo(other: Byte): Int = value.compareTo(other)

    operator fun compareTo(other: Short): Int = value.compareTo(other)

    operator fun compareTo(other: Int): Int = value.compareTo(other)

    operator fun compareTo(other: Long): Int = value.compareTo(other)

    operator fun compareTo(other: Float): Int = value.compareTo(other)

    operator fun compareTo(other: Double): Int = value.compareTo(other)

    override operator fun compareTo(other: ByteComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: ShortComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: IntComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: LongComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: FloatComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: DoubleComponent): Int = value.compareTo(other.value)


    operator fun plus(other: Byte): Int = value.plus(other)

    operator fun plus(other: Short): Int = value.plus(other)

    operator fun plus(other: Int): Int = value.plus(other)

    operator fun plus(other: Long): Long = value.plus(other)

    operator fun plus(other: Float): Float = value.plus(other)

    operator fun plus(other: Double): Double = value.plus(other)

    operator fun plus(other: ByteComponent): Int = value.plus(other.value)

    operator fun plus(other: ShortComponent): Int = value.plus(other.value)

    operator fun plus(other: IntComponent): Int = value.plus(other.value)

    operator fun plus(other: LongComponent): Long = value.plus(other.value)

    operator fun plus(other: FloatComponent): Float = value.plus(other.value)

    operator fun plus(other: DoubleComponent): Double = value.plus(other.value)


    operator fun minus(other: Byte): Int = value.minus(other)

    operator fun minus(other: Short): Int = value.minus(other)

    operator fun minus(other: Int): Int = value.minus(other)

    operator fun minus(other: Long): Long = value.minus(other)

    operator fun minus(other: Float): Float = value.minus(other)

    operator fun minus(other: Double): Double = value.minus(other)

    operator fun minus(other: ByteComponent): Int = value.minus(other.value)

    operator fun minus(other: ShortComponent): Int = value.minus(other.value)

    operator fun minus(other: IntComponent): Int = value.minus(other.value)

    operator fun minus(other: LongComponent): Long = value.minus(other.value)

    operator fun minus(other: FloatComponent): Float = value.minus(other.value)

    operator fun minus(other: DoubleComponent): Double = value.minus(other.value)


    operator fun times(other: Byte): Int = value.times(other)

    operator fun times(other: Short): Int = value.times(other)

    operator fun times(other: Int): Int = value.times(other)

    operator fun times(other: Long): Long = value.times(other)

    operator fun times(other: Float): Float = value.times(other)

    operator fun times(other: Double): Double = value.times(other)

    operator fun times(other: ByteComponent): Int = value.times(other.value)

    operator fun times(other: ShortComponent): Int = value.times(other.value)

    operator fun times(other: IntComponent): Int = value.times(other.value)

    operator fun times(other: LongComponent): Long = value.times(other.value)

    operator fun times(other: FloatComponent): Float = value.times(other.value)

    operator fun times(other: DoubleComponent): Double = value.times(other.value)


    operator fun div(other: Byte): Int = value.div(other)

    operator fun div(other: Short): Int = value.div(other)

    operator fun div(other: Int): Int = value.div(other)

    operator fun div(other: Long): Long = value.div(other)

    operator fun div(other: Float): Float = value.div(other)

    operator fun div(other: Double): Double = value.div(other)

    operator fun div(other: ByteComponent): Int = value.div(other.value)

    operator fun div(other: ShortComponent): Int = value.div(other.value)

    operator fun div(other: IntComponent): Int = value.div(other.value)

    operator fun div(other: LongComponent): Long = value.div(other.value)

    operator fun div(other: FloatComponent): Float = value.div(other.value)

    operator fun div(other: DoubleComponent): Double = value.div(other.value)


    operator fun rem(other: Byte): Int = value.rem(other)

    operator fun rem(other: Short): Int = value.rem(other)

    operator fun rem(other: Int): Int = value.rem(other)

    operator fun rem(other: Long): Long = value.rem(other)

    operator fun rem(other: Float): Float = value.rem(other)

    operator fun rem(other: Double): Double = value.rem(other)

    operator fun rem(other: ByteComponent): Int = value.rem(other.value)

    operator fun rem(other: ShortComponent): Int = value.rem(other.value)

    operator fun rem(other: IntComponent): Int = value.rem(other.value)

    operator fun rem(other: LongComponent): Long = value.rem(other.value)

    operator fun rem(other: FloatComponent): Float = value.rem(other.value)

    operator fun rem(other: DoubleComponent): Double = value.rem(other.value)


    operator fun inc() = this.apply { value++ }

    operator fun dec() = this.apply { value-- }


    operator fun unaryMinus(): Int = value.unaryMinus()


    operator fun rangeTo(other: Byte): IntRange = value.rangeTo(other)

    operator fun rangeTo(other: Short): IntRange = value.rangeTo(other)

    operator fun rangeTo(other: Int): IntRange = value.rangeTo(other)

    operator fun rangeTo(other: Long): LongRange = value.rangeTo(other)

    operator fun rangeTo(other: ByteComponent): IntRange = value.rangeTo(other.value)

    operator fun rangeTo(other: ShortComponent): IntRange = value.rangeTo(other.value)

    operator fun rangeTo(other: IntComponent): IntRange = value.rangeTo(other.value)

    operator fun rangeTo(other: LongComponent): LongRange = value.rangeTo(other.value)


    override fun toString(): String = "${this::class.simpleName}(value=$value)"
}