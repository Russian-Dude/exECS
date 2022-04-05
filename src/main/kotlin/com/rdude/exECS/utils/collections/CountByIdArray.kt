package com.rdude.exECS.utils.collections

class CountByIdArray(private var backingArray: IntArray = IntArray(16)){

    operator fun set(index: Int, value: Int) {
        growIfNeeded(index)
        backingArray[index] = maxOf(0, value)
    }

    fun increase(index: Int): Int {
        growIfNeeded(index)
        val newCount = backingArray[index] + 1
        backingArray[index] = newCount
        return newCount
    }

    fun decrease(index: Int): Int {
        if (backingArray.size <= index) return 0
        val newCount = backingArray[index] - 1
        backingArray[index] = newCount
        return newCount
    }

    operator fun get(index: Int): Int = if (backingArray.size <= index) 0 else backingArray[index]

    fun getUnsafe(index: Int): Int = backingArray[index]

    private inline fun growIfNeeded(toFitId: Int) {
        if (backingArray.size <= toFitId) {
            backingArray = backingArray.copyOf(backingArray.size * 2)
        }
    }
}