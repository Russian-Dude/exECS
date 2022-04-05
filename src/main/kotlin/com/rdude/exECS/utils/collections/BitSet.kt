package com.rdude.exECS.utils.collections

class BitSet(size: Int = 1) {

    private var data: LongArray = LongArray(size / 64 + 1) { 0 }
    var setAmount = 0
        private set

    operator fun get(index: Int) : Boolean  {
        val word = index ushr 6
        return if (data.size <= word) false else ((data[word] and (1L shl index)) != 0L)
    }

    operator fun set(index: Int, value: Boolean) = if (value) set(index) else clear(index)

    fun set(index: Int) {
        val word = index ushr 6
        growIfNeeded(index)
        if (!get(index)) setAmount++
        data[word] = data[word] or (1L shl index)
    }

    fun clear(index: Int) {
        val word = index ushr 6
        if (data.size <= word) return
        if (get(index)) setAmount--
        data[word] = data[word] and (1L shl index).inv()
    }

    private fun grow() {
        data = data.copyOf(data.size * 2)
    }

    private fun growIfNeeded(matchArray: Array<*>) {
        while (data.size * 64 < matchArray.size) {
            grow()
        }
    }

    private fun growIfNeeded(size: Int) {
        while (data.size * 64 <= size) {
            grow()
        }
    }

    fun clear()  {
        data.fill(0L)
        setAmount = 0
    }

}