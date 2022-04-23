package com.rdude.exECS.serialization

import com.rdude.exECS.component.Component
import kotlin.reflect.KClass

data class ComponentMapperSnapshot<T : Component>(
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

    fun toArray(emptyStartAmount: Int): Array<T?> {

        val result = java.lang.reflect.Array.newInstance(type.java, presence.sum() + emptyStartAmount) as Array<T?>

        var currentNull = nullStart
        var currentDataElement = 0
        var currentResultIndex = emptyStartAmount

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ComponentMapperSnapshot<*>) return false

        if (!presence.contentEquals(other.presence)) return false
        if (!data.contentEquals(other.data)) return false
        if (nullStart != other.nullStart) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = presence.contentHashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + nullStart.hashCode()
        result = 31 * result + type.hashCode()
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