package com.rdude.exECS.utils.collections

import com.rdude.exECS.component.RichComponent
import com.rdude.exECS.entity.Entity

/** Stores [Entities][Entity] to which [RichComponent] is plugged into.
 * Entities are stored in no particular order.*/
// does not implement collection or iterable to avoid boxing entities
class EntitiesSet {

    @JvmField
    @PublishedApi
    internal var backingArray = IntArray(16)

    @JvmField
    @PublishedApi
    internal val presenceBitSet = BitSet()

    @JvmField
    @PublishedApi
    internal var removeRequestsAmount = 0

    @JvmField
    @PublishedApi
    internal var lastIndex = -1

    var size = 0
        private set


    fun isEmpty() = size == 0

    fun isNotEmpty() = size > 0

    inline fun forEach(action: (Entity) -> Unit) {
        if (size == 0) return
        if (removeRequestsAmount > 0) forEachWithRemove(action)
        else forEach(0, lastIndex, action)
    }

    @PublishedApi
    internal inline fun forEachWithRemove(action: (Entity) -> Unit) {
        var i = 0
        while (i <= getLastIndex()) {
            var entityId = backingArray[i]
            var changed = false
            while (!presenceBitSet[entityId] && i <= getLastIndex()) {
                entityId = backingArray[lastIndex--]
                removeRequestsAmount--
                changed = true
            }
            if (changed) {
                backingArray[i] = entityId
                // If there are no more removed entities, the remaining ones can be iterated with the simple forEach
                if (removeRequestsAmount == 0) {
                    forEach(i, lastIndex, action)
                    return
                }
            }
            action.invoke(Entity(entityId))
            i++
        }
    }

    @PublishedApi
    internal inline fun forEach(startIndex: Int, endIndex: Int, action: (Entity) -> Unit) {
        for (i in startIndex..endIndex) {
            action(Entity(backingArray[i]))
        }
    }

    @PublishedApi
    internal fun getLastIndex() = lastIndex

    /** @return first presented [Entity] or [Entity.NO_ENTITY] if there are no Entities.*/
    fun first(): Entity {
        if (size == 0) return Entity.NO_ENTITY
        for (i in 0..lastIndex) {
            val element = backingArray[i]
            if (presenceBitSet[element]) return Entity(element)
        }
        return Entity.NO_ENTITY
    }

    operator fun contains(entity: Entity): Boolean = presenceBitSet[entity.id]

    /** @return the first [Entity] matching the given predicate, or [Entity.NO_ENTITY] if no such Entity was found.*/
    inline fun find(predicate: (Entity) -> Boolean): Entity {
        if (size == 0) return Entity.NO_ENTITY
        if (removeRequestsAmount == 0) {
            for (i in 0..lastIndex) {
                val entity = Entity(backingArray[i])
                if (predicate.invoke(entity)) return entity
            }
            return Entity.NO_ENTITY
        }
        else {
            var result = Entity.NO_ENTITY
            forEach { if (predicate.invoke(it)) result = it }
            return result
        }
    }

    internal fun add(element: Int): Boolean {
        if (presenceBitSet[element]) return false
        if (backingArray.size <= lastIndex) {
            grow()
        }
        size++
        backingArray[++lastIndex] = element
        presenceBitSet.set(element)
        return true
    }

    internal fun remove(element: Int) {
        if (!presenceBitSet[element]) return
        presenceBitSet.clear(element)
        removeRequestsAmount++
        size--
        if (size == 0) {
            lastIndex = -1
            removeRequestsAmount = 0
        }
    }

    internal fun replace(element: Int, to: Int) {
        if (!presenceBitSet[element]) return
        for (i in 0..size - 1) {
            if (backingArray[i] == element) {
                backingArray[i] = to
                presenceBitSet[element] = false
                presenceBitSet[to] = true
                break
            }
        }
    }

    internal fun clear() {
        size = 0
        lastIndex = -1
        backingArray.fill(0)
        presenceBitSet.clear()
    }

    internal fun grow() {
        val newSize = size * 2
        backingArray = backingArray.copyOf(newSize)
    }


}