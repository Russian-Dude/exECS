package com.rdude.exECS.utils.collections

/**
 * Unsafe version of BitSet.
 * IMPORTANT: this implementation will not grow by itself and need to be reassigned with a bigger copy manually.
 */
@JvmInline
value class UnsafeBitSet(val data: LongArray = longArrayOf(0)) {

    constructor(size: Int) : this(LongArray(size / 64 + 1))

    inline operator fun get(index: Int) : Boolean  {
        val word = index ushr 6
        return (data[word] and (1L shl index)) != 0L
    }

    inline operator fun set(index: Int, value: Boolean) = if (value) set(index) else clear(index)

    inline fun set(index: Int) {
        val word = index ushr 6
        data[word] = data[word] or (1L shl index)
    }

    inline fun clear(index: Int) {
        val word = index ushr 6
        data[word] = data[word] and (1L shl index).inv()
    }

    inline fun getGrowCopy() : UnsafeBitSet = UnsafeBitSet(data.copyOf(data.size * 2))

    internal inline fun getGrowCopyIfNeeded(matchArray: Array<*>) : UnsafeBitSet {
        var result = this
        while (result.data.size * 64 < matchArray.size) {
            result = getGrowCopy()
        }
        return result
    }

    internal inline fun getGrowCopyIfNeeded(size: Int) : UnsafeBitSet {
        var result = this
        while (result.data.size * 64 < size) {
            result = getGrowCopy()
        }
        return result
    }

    inline fun clear() = data.fill(0L)

}
