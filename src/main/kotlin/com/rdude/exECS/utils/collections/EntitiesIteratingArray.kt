package com.rdude.exECS.utils.collections

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityComparator
import com.rdude.exECS.utils.swap
import kotlin.math.max

/** Stores Entities. Removing and sorting of Entities are lazy and performed while iterating with [forEach] if needed.
 * Sorting is optimized to perform better on already sorted or almost sorted elements.*/
internal class EntitiesIteratingArray(
    initialCapacity: Int,
    private val comparator: EntityComparator = EntityComparator.DO_NOT_COMPARE
) {

    private var backingArray = IntArray(16)
    private val presence = UnsafeBitSet(initialCapacity)
    private var size = 0

    private val ordered = comparator != EntityComparator.DO_NOT_COMPARE
    private var isSortRequired = false

    private var hasRemoveRequests = false
    private var removeRequestsAmount = 0
    private val markedToRemove = UnsafeBitSet(initialCapacity)


    fun add(entity: Entity) = add(entity.id)

    fun add(entityId: Int) {
        if (markedToRemove[entityId]) {
            markedToRemove.clear(entityId)
            removeRequestsAmount--
            hasRemoveRequests = removeRequestsAmount > 0
            return
        }
        if (ordered) addOrdered(entityId)
        else addUnordered(entityId)
    }

    private inline fun addOrdered(entityId: Int) {
        addUnordered(entityId)
        if (isSortRequired || size == 1) {
            return
        }
        val lastEntity = backingArray[size - 2]
        if (comparator.compare(Entity(lastEntity), Entity(entityId)) > 0) {
            isSortRequired = true
        }
    }

    private inline fun addUnordered(entityId: Int) {
        if (backingArray.size == size) {
            grow()
        }
        backingArray[size++] = entityId
        presence.set(entityId)
    }

    fun requestRemove(entity: Entity) = requestRemove(entity.id)

    fun requestRemove(entityId: Int) {
        hasRemoveRequests = true
        markedToRemove.set(entityId)
        removeRequestsAmount++
    }

    fun changeOccurred() {
        isSortRequired = true
    }

    fun clear() {
        presence.clear()
        size = 0
        isSortRequired = false
        hasRemoveRequests = false
        removeRequestsAmount = 0
        markedToRemove.clear()
    }

    fun isEmpty() = size < 1

    fun isNotEmpty() = size > 0

    inline fun forEach(action: (entityId: Int) -> Unit) {
        if (removeRequestsAmount == size) {
            clear()
            return
        }
        if (ordered) {
            if (isSortRequired) {
                if (hasRemoveRequests) {
                    sortWithRemove()
                    hasRemoveRequests = false
                }
                else sortSimple()
                isSortRequired = false
                forEachSimple(0, size - 1, action)
            }
            else if (hasRemoveRequests) {
                hasRemoveRequests = false
                forEachWithRemoveOrdered(action)
            }
            else forEachSimple(0, size - 1, action)
        }
        else if (hasRemoveRequests) {
            hasRemoveRequests = false
            forEachWithRemoveUnordered(action)
        }
        else forEachSimple(0, size - 1, action)
    }

    private inline fun forEachSimple(startIndex: Int, lastIndex: Int, action: (entityId: Int) -> Unit) {
        for (i in startIndex..lastIndex) {
            action.invoke(backingArray[i])
        }
    }

    private inline fun forEachWithRemoveUnordered(action: (entityId: Int) -> Unit) {
        val lastIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(size - 1, 0)
        for (i in 0..lastIndex) {
            if (removeRequestsAmount == 0) {
                forEachSimple(i, lastIndex, action)
                return
            }
            val entityId = backingArray[i]
            if (markedToRemove[entityId]) {
                actualRemove(entityId)
                backingArray[i] = backingArray[size]
                continue
            }
            action.invoke(entityId)
        }
    }

    private inline fun forEachWithRemoveOrdered(action: (entityId: Int) -> Unit) {
        val lastIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(size - 1, 0)
        var currentIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(0, size - 1)
        var shift = currentIndex

        while (currentIndex <= lastIndex) {
            val entityId = backingArray[currentIndex]
            if (markedToRemove[entityId]) {
                actualRemove(entityId)
                shift++
                continue
            }
            if (shift > 0) {
                backingArray[currentIndex - shift] = entityId
            }
            action.invoke(entityId)
            currentIndex++
        }
    }

    private fun actualRemove(entityId: Int) {
        size--
        markedToRemove.clear(entityId)
        presence.clear(entityId)
        removeRequestsAmount--
    }


    private fun selectionSortWithRemove() {

        // current possible structure: x . x . x . x
        // x - possibly removed
        // . - possibly NOT removed

        var lastIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(size - 1, 0)
        var index1 = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(0, lastIndex)
        var index2 = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(index1 + 1, lastIndex)

        var smallestIndex = if (comparator.compare(backingArray[index1], backingArray[index2]) < 0) index1 else index2
        var i = index2 + 1

        // current possible structure: x i1 x i2 x . x li

        // first iteration with removing gaps after index2
        while (i <= lastIndex) {
            var entityId = backingArray[i]
            if (markedToRemove[entityId]) {
                actualRemove(entityId)
                val lastEntity = backingArray[lastIndex]
                backingArray[i] = lastEntity
                entityId = lastEntity
                lastIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(lastIndex - 1, i)
            }
            smallestIndex = if (comparator.compare(backingArray[smallestIndex], entityId) < 0) smallestIndex else i
            i++
        }
        if (index1 > 0) {
            backingArray[0] = backingArray[smallestIndex]
            if (smallestIndex == index1) index1 = index2 - 1
            else {
                index1 = index2 + 1
                index2 += 2
            }
        }
        else {
            backingArray.swap(0, smallestIndex)
            if (index2 == 1) {
                index1 = 1
                index2 = 2
            }
            else index1 = index2 - 1
        }

        // current possible structure: . x i1 i2 . li

        i = 1
        while (index2 <= lastIndex) {
            var smallestEntity = backingArray[index1]
            for (j in index2..lastIndex) {
                val currentEntity = backingArray[j]
                val compare = comparator.compare(smallestEntity, currentEntity)
                smallestEntity = if (compare < 0) smallestEntity else currentEntity
            }
            backingArray[i] = smallestEntity
            i++
            index1++
            index2++
        }
    }

    private fun selectionSort(startIndex: Int, endIndex: Int) {
        if (endIndex - startIndex < 1) return
        for (i in startIndex .. endIndex - 1) {
            var smallestIndex = i
            var smallestEntity = backingArray[i]
            for (j in i + 1 .. endIndex) {
                val currentEntity = backingArray[j]
                if (comparator.compare(currentEntity, smallestEntity) < 0) {
                    smallestIndex = j
                    smallestEntity = currentEntity
                }
            }
            if (smallestIndex != i) {
                backingArray.swap(i, smallestIndex)
            }
        }
    }

    private fun quickSort(startIndex: Int, endIndex: Int) {

        val length = endIndex - startIndex

        if (length <= SELECTION_SORT_MAX) {
            selectionSort(startIndex, endIndex)
            return
        }

        val pivotIndex = median5(startIndex, endIndex)
        val pivotHolderIndex = endIndex - 1
        val pivotEntity = backingArray[pivotIndex]
        val endEntity = backingArray[endIndex]

        backingArray.swap(pivotIndex, pivotHolderIndex)

        var leftStart = startIndex + 1
        var rightStart = pivotHolderIndex - 1
        var foundFromLeftIndex = leftStart
        var foundFromRightIndex = rightStart

        var leftPartSortedEndIndex = startIndex
        var rightPartSortedStartIndex =
            if(comparator.compare(endEntity, backingArray[rightStart]) >= 0) pivotHolderIndex
            else Int.MAX_VALUE

        while (true) {

            var previousEntity = backingArray[leftStart - 1]
            var currentLeftIndex = leftStart

            while (currentLeftIndex <= pivotHolderIndex) {
                val currentEntity = backingArray[currentLeftIndex]
                if (comparator.compare(currentEntity, pivotEntity) >= 0) {
                    foundFromLeftIndex = currentLeftIndex
                    leftStart = currentLeftIndex
                    break
                }
                else if (currentLeftIndex - leftPartSortedEndIndex == 1 && comparator.compare(currentEntity, previousEntity) >= 0) {
                    leftPartSortedEndIndex++
                    previousEntity = currentEntity
                }
                currentLeftIndex++
            }

            if (leftPartSortedEndIndex == pivotHolderIndex - 1) {
                return
            }

            if (foundFromLeftIndex >= rightStart) {
                break
            }

            previousEntity = backingArray[rightStart + 1]
            var currentRightIndex = rightStart
            while (currentRightIndex >= foundFromLeftIndex - 1) {
                val currentEntity = backingArray[currentRightIndex]
                if (comparator.compare(currentEntity, pivotEntity) < 0) {
                    foundFromRightIndex = currentRightIndex
                    rightStart = currentRightIndex
                    break
                }
                else if (rightPartSortedStartIndex == pivotHolderIndex
                    || (rightPartSortedStartIndex - currentRightIndex == 1 && comparator.compare(currentEntity, previousEntity) <= 0)
                ) {
                    rightPartSortedStartIndex--
                    previousEntity = currentEntity
                }
                currentRightIndex--
            }

            if (currentRightIndex <= currentLeftIndex) {
                break
            }

            backingArray.swap(foundFromLeftIndex, foundFromRightIndex)
        }

        backingArray.swap(foundFromLeftIndex, pivotHolderIndex)

        if (foundFromLeftIndex - leftPartSortedEndIndex > 1) {
            quickSort(startIndex, foundFromLeftIndex - 1)
        }

        if (rightPartSortedStartIndex - foundFromLeftIndex > 1
            || comparator.compare(backingArray[pivotHolderIndex - 1], backingArray[pivotHolderIndex]) > 0
            || comparator.compare(backingArray[pivotHolderIndex], endEntity) > 0
        ) {
            quickSort(foundFromLeftIndex + 1, endIndex)
        }
    }

    private fun removeOrdered() {
        val lastIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(size - 1, 0)
        val firstIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(0, lastIndex)
        var shift = firstIndex

        for (i in firstIndex..lastIndex) {
            val entityId = backingArray[i]
            if (markedToRemove[entityId]) {
                actualRemove(entityId)
                shift++
            }
            else if (shift > 0) {
                if (removeRequestsAmount == 0) {
                    for (j in i..lastIndex) {
                        backingArray[j - shift] = backingArray[j]
                    }
                    return
                }
                backingArray[i - shift] = entityId
            }
        }
    }

    private fun quickSortWithRemove() {
        val lastIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(size - 1, 0)
        val firstIndex = findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(0, lastIndex)
        var shift = firstIndex

        for (i in firstIndex..lastIndex) {
            val entityId = backingArray[i]
            if (markedToRemove[entityId]) {
                actualRemove(entityId)
                shift++
            }
            else if (shift > 0) {
                if (removeRequestsAmount == 0) {
                    for (j in i..lastIndex) {
                        backingArray[j - shift] = backingArray[j]
                    }
                    break
                }
                backingArray[i - shift] = entityId
            }
        }

        quickSort(0, size - 1)
    }

    private fun sortSimple() {
        if (size <= 1) return
        if (size <= SELECTION_SORT_MAX) {
            selectionSort(0, size - 1)
            return
        }
        quickSort(0, size - 1)
    }

    private fun sortWithRemove() {
        if (size <= 1) return

        val willNotBeRemovedAmount = size - removeRequestsAmount

        if (willNotBeRemovedAmount == 0) {
            clear()
            return
        }

        if (willNotBeRemovedAmount == 1) {
            for (i in 0..backingArray.size - 1) {
                val entityId = backingArray[i]
                if (!markedToRemove[entityId]) {
                    backingArray[0] = entityId
                    size = 1
                    removeRequestsAmount = 0
                    markedToRemove.clear()
                    presence.clear()
                    presence.set(entityId)
                    return
                }
            }
            throwCorruptedState()
        }

        if (willNotBeRemovedAmount == 2) {
            var firstEntity = -1
            for (i in 0..backingArray.size - 1) {
                val entityId = backingArray[i]
                if (!markedToRemove[entityId]) {
                    if (firstEntity < 0) {
                        firstEntity = entityId
                        continue
                    }
                    if (comparator.compare(firstEntity, entityId) < 0) {
                        backingArray[0] = firstEntity
                        backingArray[1] = entityId
                    }
                    else {
                        backingArray[0] = entityId
                        backingArray[1] = firstEntity
                    }
                    size = 2
                    removeRequestsAmount = 0
                    markedToRemove.clear()
                    presence.clear()
                    presence.set(firstEntity)
                    presence.set(entityId)
                    return
                }
            }
            throwCorruptedState()
        }

        if (willNotBeRemovedAmount <= SELECTION_SORT_MAX) {
            removeOrdered()
            selectionSort(0, size - 1)
            //selectionSortWithRemove()
            return
        }

        quickSortWithRemove()
    }

    fun grow() {
        backingArray = backingArray.copyOf(max(size, 1) * 2)
    }

    private fun throwCorruptedState() {
        throw RuntimeException("The state of ${this::class.simpleName} is corrupted")
    }

    private fun findFirstNotMarkedToRemoveIndexAndActuallyRemoveMarked(startIndex: Int, endIndex: Int): Int {
        if (startIndex <= endIndex) {
            for (i in startIndex..endIndex) {
                val entityId = backingArray[i]
                if (markedToRemove[entityId]) {
                    actualRemove(entityId)
                }
                else return i
            }
        }
        else {
            for (i in startIndex downTo endIndex) {
                val entityId = backingArray[i]
                if (markedToRemove[entityId]) {
                    actualRemove(entityId)
                }
                else return i
            }
        }
        throwCorruptedState()
        return -1
    }


    private inline fun EntityComparator.compare(id1: Int, id2: Int): Int = compare(Entity(id1), Entity(id2))

    private fun median5(index1: Int, index5: Int): Int {
        val index3 = index1 + (index5 - index1) / 2
        val index2 = index1 + (index3 - index1) / 2
        val index4 = index5 - (index5 - index3) / 2

        var smallestEntity = backingArray[index1]

        var currentEntity = backingArray[index2]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            smallestEntity = currentEntity
            backingArray.swap(index1, index2)
        }
        currentEntity = backingArray[index3]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            smallestEntity = currentEntity
            backingArray.swap(index1, index3)
        }
        currentEntity = backingArray[index4]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            smallestEntity = currentEntity
            backingArray.swap(index1, index4)
        }
        currentEntity = backingArray[index5]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            backingArray.swap(index1, index5)
        }

        smallestEntity = backingArray[index2]
        currentEntity = backingArray[index3]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            smallestEntity = currentEntity
            backingArray.swap(index2, index3)
        }
        currentEntity = backingArray[index4]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            smallestEntity = currentEntity
            backingArray.swap(index2, index4)
        }
        currentEntity = backingArray[index5]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            backingArray.swap(index2, index5)
        }

        smallestEntity = backingArray[index3]
        currentEntity = backingArray[index4]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            smallestEntity = currentEntity
            backingArray.swap(index3, index4)
        }
        currentEntity = backingArray[index5]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            backingArray.swap(index3, index5)
        }

        smallestEntity = backingArray[index4]
        currentEntity = backingArray[index5]
        if (comparator.compare(currentEntity, smallestEntity) < 0) {
            backingArray.swap(index4, index5)
        }

        return index3
    }


    private companion object {

        private const val SELECTION_SORT_MAX = 8

    }

}