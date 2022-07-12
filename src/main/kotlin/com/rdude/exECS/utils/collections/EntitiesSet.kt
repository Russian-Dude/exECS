package com.rdude.exECS.utils.collections

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityMethods

class EntitiesSet {

    private var backingArray = IntArray(16)

    private val presenceBitSet = BitSet()

    var size = 0
        private set

    private var currentIteration = 0


    internal fun add(element: Int): Boolean {
        if (presenceBitSet[element]) return false
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = element
        presenceBitSet.set(element)
        return true
    }

    internal fun remove(element: Int) {
        if (!presenceBitSet[element]) return
        var index = -1
        for (i in 0..size - 1) {
            if (backingArray[i] == element) {
                index = i
                break
            }
        }
        if (index == size - 1) {
            backingArray[index] = 0
        } else {
            for (i in index..size - 2) {
                backingArray[i] = backingArray[i + 1]
            }
        }
        presenceBitSet[element] = false
        size--
    }

    internal fun clear() {
        size = 0
        backingArray.fill(0)
        presenceBitSet.clear()
    }

    fun isEmpty() = size == 0

    fun isNotEmpty() = size > 0

    fun toIdArray(): IntArray = backingArray.copyOf(size)

    inline fun forEach(action: (Entity) -> Unit) {
        `start iteration`()
        for (i in 0..size - 1) {
            action(EntityMethods.wrapId(`next iteration`()))
        }
    }

    inline fun forEachIndexed(action: (index: Int, Entity) -> Unit) {
        `start iteration`()
        for (i in 0..size - 1) {
            action(i, EntityMethods.wrapId(`next iteration`()))
        }
    }

    /** @throws [NoSuchElementException] if set is empty */
    fun first(): Entity {
        if (size == 0) throw NoSuchElementException("EntitiesSet is empty")
        return Entity(backingArray[0])
    }

    /** This method should not be called directly. It needs to allow inline [forEach] without making backing array public
     *  and without boxing of [Entity].
     *  @throws [IndexOutOfBoundsException]*/
    fun `next iteration`() = backingArray[currentIteration++]

    /** This method should not be called directly. It needs to allow inline [forEach] without making backing array public
     *  and without boxing of [Entity].*/
    fun `start iteration`() {
        currentIteration = 0
    }

    internal fun grow() {
        val newSize = size * 2
        backingArray = backingArray.copyOf(newSize)
    }


}