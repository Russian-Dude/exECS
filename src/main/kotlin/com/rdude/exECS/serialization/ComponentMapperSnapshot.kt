package com.rdude.exECS.serialization

import com.rdude.exECS.component.Component
import kotlin.reflect.KClass

class ComponentMapperSnapshot<T : Component>(
    val presence: IntArray,
    val data: Array<T>,
    val nullStart: Boolean,
    val type: KClass<T>
) {

    fun toArray(toArray: Array<T?>) {

        var currentNull = nullStart
        var currentDataElement = 0
        var currentResultIndex = 0

        for (amount in presence) {
            if (currentNull) {
                currentResultIndex += amount
            } else {
                for (i in 0 until amount) {
                    toArray[currentResultIndex++] = data[currentDataElement++]
                }
            }
            currentNull = !currentNull
        }
    }

    fun toArray(): Array<T?> {

        val result = java.lang.reflect.Array.newInstance(type.java, presence.sum()) as Array<T?>

        var currentNull = nullStart
        var currentDataElement = 0
        var currentResultIndex = 0

        for (amount in presence) {
            if (currentNull) {
                currentResultIndex += amount
            } else {
                for (i in 0 until amount) {
                    result[currentResultIndex++] = data[currentDataElement++]
                }
            }
            currentNull = !currentNull
        }

        return result
    }


    companion object {

        fun <T: Component> fromArray(data: Array<out Component?>, type: KClass<T>): ComponentMapperSnapshot<T> {

            data as Array<T?>

            val presenceResult = mutableListOf<Int>()
            val dataResult = mutableListOf<T>()
            val nullStart = data[0] == null

            var current = nullStart
            var count = 0

            for (element in data) {
                if ((element == null) == current) {
                    count++
                    if (element != null) {
                        dataResult.add(element)
                    }
                } else {
                    presenceResult.add(count)
                    current = !current
                    count = 1
                    if (element != null) {
                        dataResult.add(element)
                    }
                }
            }
            presenceResult.add(count)

            val dataResultArray = java.lang.reflect.Array.newInstance(type.java, dataResult.size) as Array<T>
            dataResult.forEachIndexed { index, t -> dataResultArray[index] = t }

            return ComponentMapperSnapshot(presenceResult.toIntArray(), dataResultArray, nullStart, type)
        }
    }

}