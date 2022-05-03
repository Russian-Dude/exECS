package com.rdude.exECS

import com.rdude.exECS.utils.collections.IterableArray
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CollectionsTest {

    @Test
    fun iterableArrayRemoveWhileIterating() {
        val array = IterableArray(false, "1", "2", "3", "4", "5", "6", "7")

        array.iterate({ println(it) }, { it.toInt() % 2 == 0 })

        println(array.backingArray.contentToString())

        assert(array[0] == "1" && array[1] == "7" && array[2] == "3" && array[3] == "5" && array[4] == null)

    }

    @Test
    fun iterableArrayRemoveWhileIterating2() {
        val array = IterableArray(false, "1", "2", "3", "4", "5", "6", "7")

        array.iterate({ println(it) }, { it.toInt() % 2 == 0 })

        println(array.backingArray.contentToString())

        assert(array.size == 4)

    }


    @Test
    fun iterableArrayRemoveWhileIterating3() {
        val array = IterableArray(false, "1", "2", "3", "4", "5", "6", "7")

        var returned = false
        array.iterate(
            onEach = {
                returned = it.toInt() % 2 == 0
            },
            removeIf = { returned }
        )

        println(array.backingArray.contentToString())

        assert(array.size == 4)

    }

    @Test
    fun iterableArrayRemoveContainingOrder() {
        val array = IterableArray(false, "1", "2", "3", "4", "5")
        array.removeContainingOrder("3")
        array.add("717")
        println(array)
    }

/*    @Test
    fun compressedArray() {

        val array = arrayOf(null, "1", "2", null, "3", null, null, null, "4", "5", null, "6", "7", null, null)

        val compressed = CompressedArray.fromArray(array)

        println("start: ${compressed.nullStart}")
        println("presence: ${compressed.presence.contentToString()}")
        println("data: ${compressed.data.contentToString()}")

        val backToArray = compressed.toArray()

        println("back to array: ${backToArray.contentToString()}")

        assert(array.contentEquals(backToArray))
    }*/

}