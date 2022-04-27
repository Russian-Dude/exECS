package com.rdude.exECS.utils.collections

class IntResizableArray (private var backingArray: IntArray = IntArray(16)) {

    operator fun set(index: Int, value: Int) {
        growIfNeeded(index)
        backingArray[index] = value
    }

    operator fun get(index: Int): Int = backingArray[index]


    private inline fun growIfNeeded(toFitId: Int) {
        if (backingArray.size <= toFitId) {
            backingArray = backingArray.copyOf(backingArray.size * 2)
        }
    }

}