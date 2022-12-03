package com.rdude.exECS

import com.rdude.exECS.utils.collections.EntitiesIterableArray
import com.rdude.exECS.utils.swap
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntitiesIterableArrayUnorderedTest {

    private val random = Random(717)
    private val array = EntitiesIterableArray(64_000)
    private val possibleElements = run {
        generateSequence { random.nextInt(0, 64_000) }
            .take(128)
            .toList()
    }



    @BeforeEach
    fun clearArray() {
        array.clear()
    }

    @Test
    fun simpleIteration() {
        possibleElements.forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.forEach { result.add(it) }
        assert(result == possibleElements)
    }

    @Test
    fun iterationWithRemoveLeftElements() {
        possibleElements.forEach { array.add(it) }
        possibleElements.take(10).forEach { array.requestRemove(it) }
        val result = ArrayList<Int>(128)
        val correct = possibleElements.drop(10)
        array.forEach { result.add(it) }
        assert(result == correct)
    }

    @Test
    fun iterationWithRemoveRightElements() {
        possibleElements.forEach { array.add(it) }
        possibleElements.takeLast(10).forEach { array.requestRemove(it) }
        val result = ArrayList<Int>(128)
        val correct = possibleElements.dropLast(10)
        array.forEach { result.add(it) }
        assert(result == correct)
    }

    @Test
    fun iterationWithRemoveMiddleElements() {
        possibleElements.forEach { array.add(it) }
        possibleElements.drop(25).take(25).forEach { array.requestRemove(it) }
        val result = ArrayList<Int>(128)
        val correct = possibleElements.subList(0, 25) + possibleElements.subList(50, possibleElements.size)
        array.forEach { result.add(it) }
        assert(result == correct)
    }

    @Test
    fun iterationWithRemoveAllElements() {
        possibleElements.forEach { array.add(it) }
        possibleElements.forEach { array.requestRemove(it) }
        array.forEach { /* because of lazy removal */ }
        assert(array.isEmpty())
    }

    @Test
    fun iterationWithRemoveRandomElements() {
        possibleElements.forEach { array.add(it) }
        val random = Random(717)
        val correct = possibleElements
            .mapNotNull {
                if (random.nextBoolean()) {
                    array.requestRemove(it)
                    null
                }
                else it
            }
        val result = ArrayList<Int>(128)
        array.forEach { result.add(it) }
        assert(result == correct)
    }

}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntitiesIterableArrayOrderedTest {

    private val random = Random(217)
    private val values = IntArray(128)
    private val array = EntitiesIterableArray(128) { e1, e2 -> values[e1.id].compareTo(values[e2.id]) }



    @BeforeEach
    fun clearArray() {
        array.clear()
    }

    @Test
    fun randomElementsSmall() {
        repeat(7) {
            values[it] = random.nextInt()
            array.add(it)
        }
        val result = ArrayList<Int>(7)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.take(7).sorted())
    }

    @Test
    fun randomElementsBig() {
        repeat(values.size) { values[it] = random.nextInt(1, 25) }
        val required = values.sorted()
        for (i in 0..1000) {
            array.clear()
            values.shuffle(random)
            repeat(values.size) {
                array.add(it)
            }
            array.forceChangeOccurred()
            val result = ArrayList<Int>(values.size)
            array.forEach { result.add(values[it]) }
            assert(result == required)
        }
    }

    @Test
    fun ascendingElementsSmall() {
        repeat(7) {
            values[it] = it
            array.add(it)
        }
        val result = ArrayList<Int>(7)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.take(7))
    }

    @Test
    fun ascendingElementsBig() {
        repeat(values.size) {
            values[it] = it
            array.add(it)
        }
        val result = ArrayList<Int>(values.size)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.toList())
    }

    @Test
    fun descendingElementsSmall() {
        repeat(7) {
            values[it] = 7 - it
            array.add(it)
        }
        val result = ArrayList<Int>(7)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.take(7).reversed())
    }

    @Test
    fun descendingElementsBig() {
        repeat(values.size) {
            values[it] = values.size - it
            array.add(it)
        }
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.toList().reversed())
    }

    @Test
    fun ascendingElementsFirstAndEndSwappedBig() {
        repeat(values.size) {
            values[it] = it
            array.add(it)
        }
        values.swap(0, values.size - 1)
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun ascendingElementsMiddleChangedBig() {
        repeat(values.size) {
            values[it] = it
            array.add(it)
        }
        val l = values.size / 2 - values.size / 6
        val r = values.size / 2 + values.size / 6
        for (i in l..r) {
            values[i] = random.nextInt()
        }
        val result = ArrayList<Int>(values.size)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun ascendingElementsRandomChangedBig() {
        repeat(values.size) {
            values[it] = it
            array.add(it)
        }
        repeat(values.size / 10) {
            values[random.nextInt(0, values.size)] = random.nextInt()
        }
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun descendingElementsFirstAndEndSwappedBig() {
        repeat(values.size) {
            values[it] = values.size - it
            array.add(it)
        }
        values.swap(0, values.size - 1)
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun descendingElementsMiddleChangedBig() {
        repeat(values.size) {
            values[it] = values.size - it
            array.add(it)
        }
        val l = values.size / 2 - values.size / 6
        val r = values.size / 2 + values.size / 6
        for (i in l..r) {
            values[i] = random.nextInt()
        }
        val result = ArrayList<Int>(values.size)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun descendingElementsRandomChangedBig() {
        repeat(values.size) {
            values[it] = values.size - it
            array.add(it)
        }
        repeat(values.size / 10) {
            values[random.nextInt(0, values.size)] = random.nextInt()
        }
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun randomElementsRemoveFirstSmall() {
        repeat(7) {
            values[it] = random.nextInt()
            array.add(it)
        }
        repeat(3) { array.requestRemove(it) }
        val result = ArrayList<Int>(7)
        array.forEach { result.add(values[it]) }
        assert(result == values.drop(3).take(4).sorted())
    }

    @Test
    fun randomElementsRemoveLastSmall() {
        repeat(7) {
            values[it] = random.nextInt()
            array.add(it)
        }
        repeat(3) { array.requestRemove(6 - it) }
        val result = ArrayList<Int>(7)
        array.forEach { result.add(values[it]) }
        assert(result == values.take(7).dropLast(3).sorted())
    }

    @Test
    fun randomElementsRemoveRandomSmall() {
        repeat(7) {
            values[it] = random.nextInt()
            array.add(it)
        }
        val required = ArrayList<Int>()
        repeat(7) {
            if (it % 2 == 0) array.requestRemove(it)
            else required.add(values[it])
        }
        val result = ArrayList<Int>(7)
        array.forEach { result.add(values[it]) }
        assert(result == required.sorted())
    }

    @Test
    fun equalBig() {
        repeat(values.size) {
            values[it] = 717
            array.add(it)
        }
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.toList())
    }

    @Test
    fun twoValuesRandomPositionsBig() {
        repeat(values.size) {
            values[it] = if (random.nextBoolean()) 1 else 0
            array.add(it)
        }
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun twoValuesAscendingBig() {
        val midIndex = values.size / 2
        repeat(values.size) {
            values[it] = if (it < midIndex) 0 else 1
            array.add(it)
        }
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun twoValuesDescendingBig() {
        val midIndex = values.size / 2
        repeat(values.size) {
            values[it] = if (it < midIndex) 1 else 0
            array.add(it)
        }
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }

    @Test
    fun equalExceptFirstAscendingBig() {
        repeat(values.size) {
            values[it] = 717
            array.add(it)
        }
        values[0] = 0
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.toList())
    }

    @Test
    fun equalExceptFirstDescendingBig() {
        repeat(values.size) {
            values[it] = 717
            array.add(it)
        }
        values[values.size - 1] = 0
        val result = ArrayList<Int>(128)
        array.forceChangeOccurred()
        array.forEach { result.add(values[it]) }
        assert(result == values.sorted())
    }
}