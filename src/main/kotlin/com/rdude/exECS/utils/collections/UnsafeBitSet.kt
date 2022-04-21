package com.rdude.exECS.utils.collections

/**
 * Unsafe version of BitSet.
 * To reduce size checks underlying data array will grow only when asked from outside.
 */
internal class UnsafeBitSet(size: Int = 1) {

    internal var data: LongArray = LongArray(size / 64 + 1) { 0 }

    internal operator fun get(index: Int) : Boolean  {
        val word = index ushr 6
        return (data[word] and (1L shl index)) != 0L
    }

    internal operator fun set(index: Int, value: Boolean) = if (value) set(index) else clear(index)

    internal fun set(index: Int) {
        val word = index ushr 6
        data[word] = data[word] or (1L shl index)
    }

    internal fun clear(index: Int) {
        val word = index ushr 6
        data[word] = data[word] and (1L shl index).inv()
    }

    internal fun grow() {
        data = data.copyOf(data.size * 2)
    }

    internal fun growIfNeeded(matchArray: Array<*>) {
        while (data.size * 64 < matchArray.size) {
            grow()
        }
    }

    internal fun growIfNeeded(size: Int) {
        while (data.size * 64 < size) {
            grow()
        }
    }

    internal fun clear() = data.fill(0L)


    internal inline fun getTrueValues(): List<Int> {
        val result = mutableListOf<Int>()
        var index = 0
        for (word in data) {
            if (word == 0L) {
                index += 64
                continue
            }
            for (i in 0..63) {
                if ((word and (1L shl i)) != 0L) {
                    result.add(index)
                }
                index++
            }
        }
        return result
    }

}
