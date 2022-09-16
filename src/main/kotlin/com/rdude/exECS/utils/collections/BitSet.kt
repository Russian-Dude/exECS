package com.rdude.exECS.utils.collections

class BitSet(size: Int = 1) {

    private var data: LongArray = LongArray(size / 64 + 1) { 0 }

    operator fun get(index: Int) : Boolean  {
        val word = index ushr 6
        return if (data.size <= word) false else ((data[word] and (1L shl index)) != 0L)
    }

    operator fun set(index: Int, value: Boolean) = if (value) set(index) else clear(index)

    fun set(index: Int) {
        val word = index ushr 6
        growIfNeeded(index)
        data[word] = data[word] or (1L shl index)
    }

    fun clear(index: Int) {
        val word = index ushr 6
        if (data.size <= word) return
        data[word] = data[word] and (1L shl index).inv()
    }

    fun clear()  {
        data.fill(0L)
    }

    private fun grow(newSize: Int) {
        data = data.copyOf(newSize)
    }

    private fun growIfNeeded(matchArray: Array<*>) {
        if (data.size shl 6 < matchArray.size) {
            grow(getNewSize(data.size, matchArray.size ushr 6))
        }
    }

    private fun growIfNeeded(size: Int) {
        if (data.size shl 6 < size) {
            grow(getNewSize(data.size, size ushr 6))
        }
    }

    private fun getNewSize(oldSize: Int, minSize: Int): Int {
        var newSize = oldSize
        while (newSize < minSize) {
            newSize = newSize shl 1
            if (newSize < oldSize) {
                newSize = minSize + 1
            }
        }
        return newSize
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BitSet
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }


}