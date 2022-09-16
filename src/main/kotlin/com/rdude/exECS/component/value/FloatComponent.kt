package com.rdude.exECS.component.value

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.observable.ObservableFloatComponent

/** Component representing some [Float] value. Allows to perform some actions directly on the component without the need
 *  to access the value.
 * ```
 * myComponent += 717 // math operators with primitives
 * myComponent /= myAnotherComponent // math operators with another primitive components
 * maxOf(myComponent, myAnotherComponent) // comparable by value
 * 0..myComponent // creating ranges
 * ```
 * @see [ObservableFloatComponent]*/
abstract class FloatComponent(open var value: Float = 0.0F) : Component, Comparable<FloatComponent> {

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

    override operator fun compareTo(other: FloatComponent): Int = value.compareTo(other.value)

    operator fun compareTo(other: DoubleComponent): Int = value.compareTo(other.value)


    operator fun plus(other: Byte): Float = value.plus(other)

    operator fun plus(other: Short): Float = value.plus(other)

    operator fun plus(other: Int): Float = value.plus(other)

    operator fun plus(other: Long): Float = value.plus(other)

    operator fun plus(other: Float): Float = value.plus(other)

    operator fun plus(other: Double): Double = value.plus(other)

    operator fun plus(other: ByteComponent): Float = value.plus(other.value)

    operator fun plus(other: ShortComponent): Float = value.plus(other.value)

    operator fun plus(other: IntComponent): Float = value.plus(other.value)

    operator fun plus(other: LongComponent): Float = value.plus(other.value)

    operator fun plus(other: FloatComponent): Float = value.plus(other.value)

    operator fun plus(other: DoubleComponent): Double = value.plus(other.value)


    operator fun plusAssign(other: Byte) { value += other }

    operator fun plusAssign(other: Short) { value += other }

    operator fun plusAssign(other: Int) { value += other }

    operator fun plusAssign(other: Long) { value += other }

    operator fun plusAssign(other: Float) { value += other }

    operator fun plusAssign(other: ByteComponent) { value += other.value }

    operator fun plusAssign(other: ShortComponent) { value += other.value }

    operator fun plusAssign(other: IntComponent) { value += other.value }

    operator fun plusAssign(other: LongComponent) { value += other.value }

    operator fun plusAssign(other: FloatComponent) { value += other.value }


    operator fun minus(other: Byte): Float = value.minus(other)

    operator fun minus(other: Short): Float = value.minus(other)

    operator fun minus(other: Int): Float = value.minus(other)

    operator fun minus(other: Long): Float = value.minus(other)

    operator fun minus(other: Float): Float = value.minus(other)

    operator fun minus(other: Double): Double = value.minus(other)

    operator fun minus(other: ByteComponent): Float = value.minus(other.value)

    operator fun minus(other: ShortComponent): Float = value.minus(other.value)

    operator fun minus(other: IntComponent): Float = value.minus(other.value)

    operator fun minus(other: LongComponent): Float = value.minus(other.value)

    operator fun minus(other: FloatComponent): Float = value.minus(other.value)

    operator fun minus(other: DoubleComponent): Double = value.minus(other.value)


    operator fun minusAssign(other: Byte) { value -= other }

    operator fun minusAssign(other: Short) { value -= other }

    operator fun minusAssign(other: Int) { value -= other }

    operator fun minusAssign(other: Long) { value -= other }

    operator fun minusAssign(other: Float) { value -= other }

    operator fun minusAssign(other: ByteComponent) { value -= other.value }

    operator fun minusAssign(other: ShortComponent) { value -= other.value }

    operator fun minusAssign(other: IntComponent) { value -= other.value }

    operator fun minusAssign(other: LongComponent) { value -= other.value }

    operator fun minusAssign(other: FloatComponent) { value -= other.value }


    operator fun times(other: Byte): Float = value.times(other)

    operator fun times(other: Short): Float = value.times(other)

    operator fun times(other: Int): Float = value.times(other)

    operator fun times(other: Long): Float = value.times(other)

    operator fun times(other: Float): Float = value.times(other)

    operator fun times(other: Double): Double = value.times(other)

    operator fun times(other: ByteComponent): Float = value.times(other.value)

    operator fun times(other: ShortComponent): Float = value.times(other.value)

    operator fun times(other: IntComponent): Float = value.times(other.value)

    operator fun times(other: LongComponent): Float = value.times(other.value)

    operator fun times(other: FloatComponent): Float = value.times(other.value)

    operator fun times(other: DoubleComponent): Double = value.times(other.value)


    operator fun timesAssign(other: Byte) { value *= other }

    operator fun timesAssign(other: Short) { value *= other }

    operator fun timesAssign(other: Int) { value *= other }

    operator fun timesAssign(other: Long) { value *= other }

    operator fun timesAssign(other: Float) { value *= other }

    operator fun timesAssign(other: ByteComponent) { value *= other.value }

    operator fun timesAssign(other: ShortComponent) { value *= other.value }

    operator fun timesAssign(other: IntComponent) { value *= other.value }

    operator fun timesAssign(other: LongComponent) { value *= other.value }

    operator fun timesAssign(other: FloatComponent) { value *= other.value }


    operator fun div(other: Byte): Float = value.div(other)

    operator fun div(other: Short): Float = value.div(other)

    operator fun div(other: Int): Float = value.div(other)

    operator fun div(other: Long): Float = value.div(other)

    operator fun div(other: Float): Float = value.div(other)

    operator fun div(other: Double): Double = value.div(other)

    operator fun div(other: ByteComponent): Float = value.div(other.value)

    operator fun div(other: ShortComponent): Float = value.div(other.value)

    operator fun div(other: IntComponent): Float = value.div(other.value)

    operator fun div(other: LongComponent): Float = value.div(other.value)

    operator fun div(other: FloatComponent): Float = value.div(other.value)

    operator fun div(other: DoubleComponent): Double = value.div(other.value)


    operator fun divAssign(other: Byte) { value /= other }

    operator fun divAssign(other: Short) { value /= other }

    operator fun divAssign(other: Int) { value /= other }

    operator fun divAssign(other: Long) { value /= other }

    operator fun divAssign(other: Float) { value /= other }

    operator fun divAssign(other: ByteComponent) { value /= other.value }

    operator fun divAssign(other: ShortComponent) { value /= other.value }

    operator fun divAssign(other: IntComponent) { value /= other.value }

    operator fun divAssign(other: LongComponent) { value /= other.value }

    operator fun divAssign(other: FloatComponent) { value /= other.value }


    operator fun rem(other: Byte): Float = value.rem(other)

    operator fun rem(other: Short): Float = value.rem(other)

    operator fun rem(other: Int): Float = value.rem(other)

    operator fun rem(other: Long): Float = value.rem(other)

    operator fun rem(other: Float): Float = value.rem(other)

    operator fun rem(other: Double): Double = value.rem(other)

    operator fun rem(other: ByteComponent): Float = value.rem(other.value)

    operator fun rem(other: ShortComponent): Float = value.rem(other.value)

    operator fun rem(other: IntComponent): Float = value.rem(other.value)

    operator fun rem(other: LongComponent): Float = value.rem(other.value)

    operator fun rem(other: FloatComponent): Float = value.rem(other.value)

    operator fun rem(other: DoubleComponent): Double = value.rem(other.value)


    operator fun remAssign(other: Byte) { value %= other }

    operator fun remAssign(other: Short) { value %= other }

    operator fun remAssign(other: Int) { value %= other }

    operator fun remAssign(other: Long) { value %= other }

    operator fun remAssign(other: Float) { value %= other }

    operator fun remAssign(other: ByteComponent) { value %= other.value }

    operator fun remAssign(other: ShortComponent) { value %= other.value }

    operator fun remAssign(other: IntComponent) { value %= other.value }

    operator fun remAssign(other: LongComponent) { value %= other.value }

    operator fun remAssign(other: FloatComponent) { value %= other.value }


    operator fun inc() = this.apply { value++ }

    operator fun dec() = this.apply { value-- }


    operator fun unaryMinus(): Float = value.unaryMinus()


    operator fun rangeTo(other: Float) = value.rangeTo(other)

    operator fun rangeTo(other: FloatComponent) = value.rangeTo(other.value)


    override fun toString(): String = "${this::class.simpleName}(value=$value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FloatComponent
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }


}