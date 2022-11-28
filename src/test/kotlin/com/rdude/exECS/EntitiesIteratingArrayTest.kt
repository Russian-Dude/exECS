package com.rdude.exECS

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.utils.collections.EntitiesIteratingArray
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntitiesIteratingArrayUnorderedTest {

    private val random = Random(717)
    private val array = EntitiesIteratingArray(64_000)
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
class EntitiesIteratingArrayOrderedTest {

    private val random = Random(217)
    private val array = EntitiesIteratingArray(64_000) { e1, e2 -> e1.id - e2.id }
    private val possibleElements = run {
        generateSequence { random.nextInt(0, 50) }
            .take(128)
            .toList()
    }



    @BeforeEach
    fun clearArray() {
        array.clear()
    }

    @Test
    fun randomElementsSmall() {
        possibleElements.take(7).forEach { array.add(it) }
        val result = ArrayList<Int>(7)
        array.forEach { result.add(it) }
        assert(result == possibleElements.take(7).sorted())
    }

    @Test
    fun randomElementsBig() {
        for (i in 0..1000) {
            val random = Random(i)
            for (j in 50..1000 step 51) {
                val elements = generateSequence { random.nextInt(0, j) }
                    .take(128)
                    .toList()
                array.clear()
                elements.forEach { array.add(it) }
                val result = ArrayList<Int>(128)
                array.forEach { result.add(it) }
                assert(result == elements.sorted())
            }
        }
    }

    @Test
    fun equalElementsSmall() {
        repeat(7) { array.add(Entity(10)) }
        val result = ArrayList<Int>(7)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == IntArray(7) { 10 }.toList())
    }

    @Test
    fun equalElementsBig() {
        repeat(128) { array.add(Entity(10)) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == IntArray(128) { 10 }.toList())
    }

    @Test
    fun ascendingElementsSmall() {
        val required = IntArray(7) { it }.toList()
        required.forEach { array.add(it) }
        val result = ArrayList<Int>(7)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(required == result)
    }

    @Test
    fun ascendingElementsBig() {
        val required = IntArray(128) { it }.toList()
        required.forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(required == result)
    }

    @Test
    fun descendingElementsSmall() {
        val required = IntArray(7) { it }.toList()
        for (i in 6 downTo 0) {
            array.add(i)
        }
        val result = ArrayList<Int>(7)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(required == result)
    }

    @Test
    fun descendingElementsBig() {
        val required = IntArray(128) { it }.toList()
        for (i in 127 downTo 0) {
            array.add(i)
        }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(required == result)
    }

    @Test
    fun ascendingElementsFirstAndEndSwappedBig() {
        val elements = IntArray(128) { it }
        val tmp = elements[0]
        elements[0] = elements[127]
        elements[127] = tmp
        elements.forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun ascendingElementsMiddleChangedBig() {
        val elements = IntArray(128) { it }
        elements[64] = random.nextInt(0, 64_000)
        elements.forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun ascendingElementsMiddleChangedBig2() {
        val elements = IntArray(128) { it }
        for (i in 55..70) {
            elements[i] = random.nextInt(0, 64_000)
        }
        elements.forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun ascendingElementsRandomChangedBig() {
        val elements = IntArray(128) { it }
        for (i in 0..20) {
            elements[random.nextInt(0, 128)] = random.nextInt(0, 64_000)
        }
        elements.forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun descendingElementsFirstAndEndSwappedBig() {
        val elements = IntArray(128) { it }
        val tmp = elements[0]
        elements[0] = elements[127]
        elements[127] = tmp
        for (i in 127 downTo 0) {
             array.add(elements[i])
        }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun descendingElementsMiddleChangedBig() {
        val elements = IntArray(128) { it }
        elements[64] = random.nextInt(0, 64_000)
        elements.reversed().forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun descendingElementsMiddleChangedBig2() {
        val elements = IntArray(128) { it }
        for (i in 55..70) {
            elements[i] = random.nextInt(0, 64_000)
        }
        elements.reversed().forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun descendingElementsRandomChangedBig() {
        val elements = IntArray(128) { it }
        for (i in 0..20) {
            elements[random.nextInt(0, 128)] = random.nextInt(0, 64_000)
        }
        elements.reversed().forEach { array.add(it) }
        val result = ArrayList<Int>(128)
        array.changeOccurred()
        array.forEach { result.add(it) }
        assert(result == elements.sorted())
    }

    @Test
    fun randomElementsRemoveFirstSmall() {
        possibleElements.take(7).forEach { array.add(it) }
        repeat(3) { array.requestRemove(possibleElements[it]) }
        val result = ArrayList<Int>(7)
        array.forEach { result.add(it) }
        assert(result == possibleElements.drop(3).take(4).sorted())
    }

    @Test
    fun randomElementsRemoveLastSmall() {
        possibleElements.take(7).forEach { array.add(it) }
        repeat(3) { array.requestRemove(possibleElements[6 - it]) }
        val result = ArrayList<Int>(7)
        array.forEach { result.add(it) }
        assert(result == possibleElements.take(7).dropLast(3).sorted())
    }

    @Test
    fun randomElementsRemoveRandomSmall() {
        possibleElements.take(7).forEach { array.add(it) }
        val required = ArrayList<Int>()
        repeat(7) {
            if (it % 2 == 0) array.requestRemove(possibleElements[it])
            else required.add(possibleElements[it])
        }
        val result = ArrayList<Int>(7)
        array.forEach { result.add(it) }
        assert(result == required.sorted())
    }
}