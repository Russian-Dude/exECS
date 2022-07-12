package com.rdude.exECS.component.value

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.observable.ObservableDoubleComponent

/** Component representing some [Double] value. Allows to perform some actions directly on the component without the need
 *  to access the value.
 * ```
 * myComponent += 71.7 // math operators with primitives
 * myComponent /= myAnotherComponent // math operators with another primitive components
 * maxOf(myComponent, myAnotherComponent) // comparable by value
 * 0.0..myComponent // creating ranges
 * ```
 * @see [ObservableDoubleComponent]*/
abstract class DoubleComponent(open var value: Double = 0.0) : Component, Comparable<DoubleComponent> {

    operator fun compareTo(other: Byte): Int = value.compareTo(other)

    operator fun compareTo(other: Short): Int = value.compareTo(other)

    operator fun compareTo(other: Int): Int = value.compareTo(other)

    operator fun compareTo(other: Long): Int = value.compareTo(other)

    operator fun compareTo(other: Float): Int = value.compareTo(other)

    operator fun compareTo(other: Double): Int = value.compareTo(other)

    operator fun compareTo(other: ByteComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: ShortComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: IntComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: LongComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: FloatComponent): Int = value.compareTo(other.value)

    override operator fun compareTo(other: DoubleComponent): Int = value.compareTo(other.value)


    operator fun plus(other: Byte): Double = value.plus(other)

    operator fun plus(other: Short): Double = value.plus(other)

    operator fun plus(other: Int): Double = value.plus(other)

    operator fun plus(other: Long): Double = value.plus(other)

    operator fun plus(other: Float): Double = value.plus(other)

    operator fun plus(other: Double): Double = value.plus(other)

    operator fun plus(other: ByteComponent): Double = value.plus(other.value)

    operator fun plus(other: ShortComponent): Double = value.plus(other.value)

    operator fun plus(other: IntComponent): Double = value.plus(other.value)

    operator fun plus(other: LongComponent): Double = value.plus(other.value)

    operator fun plus(other: FloatComponent): Double = value.plus(other.value)

    operator fun plus(other: DoubleComponent): Double = value.plus(other.value)


    operator fun plusAssign(other: Byte) { value += other }

    operator fun plusAssign(other: Short) { value += other }

    operator fun plusAssign(other: Int) { value += other }

    operator fun plusAssign(other: Long) { value += other }

    operator fun plusAssign(other: Float) { value += other }

    operator fun plusAssign(other: Double) { value += other }

    operator fun plusAssign(other: ByteComponent) { value += other.value }

    operator fun plusAssign(other: ShortComponent) { value += other.value }

    operator fun plusAssign(other: IntComponent) { value += other.value }

    operator fun plusAssign(other: LongComponent) { value += other.value }

    operator fun plusAssign(other: FloatComponent) { value += other.value }

    operator fun plusAssign(other: DoubleComponent) { value += other.value }


    operator fun minus(other: Byte): Double = value.minus(other)

    operator fun minus(other: Short): Double = value.minus(other)

    operator fun minus(other: Int): Double = value.minus(other)

    operator fun minus(other: Long): Double = value.minus(other)

    operator fun minus(other: Float): Double = value.minus(other)

    operator fun minus(other: Double): Double = value.minus(other)

    operator fun minus(other: ByteComponent): Double = value.minus(other.value)

    operator fun minus(other: ShortComponent): Double = value.minus(other.value)

    operator fun minus(other: IntComponent): Double = value.minus(other.value)

    operator fun minus(other: LongComponent): Double = value.minus(other.value)

    operator fun minus(other: FloatComponent): Double = value.minus(other.value)

    operator fun minus(other: DoubleComponent): Double = value.minus(other.value)


    operator fun minusAssign(other: Byte) { value -= other }

    operator fun minusAssign(other: Short) { value -= other }

    operator fun minusAssign(other: Int) { value -= other }

    operator fun minusAssign(other: Long) { value -= other }

    operator fun minusAssign(other: Float) { value -= other }

    operator fun minusAssign(other: Double) { value -= other }

    operator fun minusAssign(other: ByteComponent) { value -= other.value }

    operator fun minusAssign(other: ShortComponent) { value -= other.value }

    operator fun minusAssign(other: IntComponent) { value -= other.value }

    operator fun minusAssign(other: LongComponent) { value -= other.value }

    operator fun minusAssign(other: FloatComponent) { value -= other.value }

    operator fun minusAssign(other: DoubleComponent) { value -= other.value }


    operator fun times(other: Byte): Double = value.times(other)

    operator fun times(other: Short): Double = value.times(other)

    operator fun times(other: Int): Double = value.times(other)

    operator fun times(other: Long): Double = value.times(other)

    operator fun times(other: Float): Double = value.times(other)

    operator fun times(other: Double): Double = value.times(other)

    operator fun times(other: ByteComponent): Double = value.times(other.value)

    operator fun times(other: ShortComponent): Double = value.times(other.value)

    operator fun times(other: IntComponent): Double = value.times(other.value)

    operator fun times(other: LongComponent): Double = value.times(other.value)

    operator fun times(other: FloatComponent): Double = value.times(other.value)

    operator fun times(other: DoubleComponent): Double = value.times(other.value)


    operator fun timesAssign(other: Byte) { value *= other }

    operator fun timesAssign(other: Short) { value *= other }

    operator fun timesAssign(other: Int) { value *= other }

    operator fun timesAssign(other: Long) { value *= other }

    operator fun timesAssign(other: Float) { value *= other }

    operator fun timesAssign(other: Double) { value *= other }

    operator fun timesAssign(other: ByteComponent) { value *= other.value }

    operator fun timesAssign(other: ShortComponent) { value *= other.value }

    operator fun timesAssign(other: IntComponent) { value *= other.value }

    operator fun timesAssign(other: LongComponent) { value *= other.value }

    operator fun timesAssign(other: FloatComponent) { value *= other.value }

    operator fun timesAssign(other: DoubleComponent) { value *= other.value }


    operator fun div(other: Byte): Double = value.div(other)

    operator fun div(other: Short): Double = value.div(other)

    operator fun div(other: Int): Double = value.div(other)

    operator fun div(other: Long): Double = value.div(other)

    operator fun div(other: Float): Double = value.div(other)

    operator fun div(other: Double): Double = value.div(other)

    operator fun div(other: ByteComponent): Double = value.div(other.value)

    operator fun div(other: ShortComponent): Double = value.div(other.value)

    operator fun div(other: IntComponent): Double = value.div(other.value)

    operator fun div(other: LongComponent): Double = value.div(other.value)

    operator fun div(other: FloatComponent): Double = value.div(other.value)

    operator fun div(other: DoubleComponent): Double = value.div(other.value)


    operator fun divAssign(other: Byte) { value /= other }

    operator fun divAssign(other: Short) { value /= other }

    operator fun divAssign(other: Int) { value /= other }

    operator fun divAssign(other: Long) { value /= other }

    operator fun divAssign(other: Float) { value /= other }

    operator fun divAssign(other: Double) { value /= other }

    operator fun divAssign(other: ByteComponent) { value /= other.value }

    operator fun divAssign(other: ShortComponent) { value /= other.value }

    operator fun divAssign(other: IntComponent) { value /= other.value }

    operator fun divAssign(other: LongComponent) { value /= other.value }

    operator fun divAssign(other: FloatComponent) { value /= other.value }

    operator fun divAssign(other: DoubleComponent) { value /= other.value }


    operator fun rem(other: Byte): Double = value.rem(other)

    operator fun rem(other: Short): Double = value.rem(other)

    operator fun rem(other: Int): Double = value.rem(other)

    operator fun rem(other: Long): Double = value.rem(other)

    operator fun rem(other: Float): Double = value.rem(other)

    operator fun rem(other: Double): Double = value.rem(other)

    operator fun rem(other: ByteComponent): Double = value.rem(other.value)

    operator fun rem(other: ShortComponent): Double = value.rem(other.value)

    operator fun rem(other: IntComponent): Double = value.rem(other.value)

    operator fun rem(other: LongComponent): Double = value.rem(other.value)

    operator fun rem(other: FloatComponent): Double = value.rem(other.value)

    operator fun rem(other: DoubleComponent): Double = value.rem(other.value)


    operator fun remAssign(other: Byte) { value %= other }

    operator fun remAssign(other: Short) { value %= other }

    operator fun remAssign(other: Int) { value %= other }

    operator fun remAssign(other: Long) { value %= other }

    operator fun remAssign(other: Float) { value %= other }

    operator fun remAssign(other: Double) { value %= other }

    operator fun remAssign(other: ByteComponent) { value %= other.value }

    operator fun remAssign(other: ShortComponent) { value %= other.value }

    operator fun remAssign(other: IntComponent) { value %= other.value }

    operator fun remAssign(other: LongComponent) { value %= other.value }

    operator fun remAssign(other: FloatComponent) { value %= other.value }

    operator fun remAssign(other: DoubleComponent) { value %= other.value }


    operator fun inc() = this.apply { value++ }

    operator fun dec() = this.apply { value-- }


    operator fun unaryMinus(): Double = value.unaryMinus()


    operator fun rangeTo(other: Double) = value.rangeTo(other)

    operator fun rangeTo(other: DoubleComponent) = value.rangeTo(other.value)


    override fun toString(): String = "${this::class.simpleName}(value=$value)"
}