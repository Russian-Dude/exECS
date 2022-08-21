package com.rdude.exECS.utils.collections

internal class PriorityArrayQueue<T> private constructor(backingArray: Array<ArrayQueue<T>>) {

    private val queuesByPriority: Array<ArrayQueue<T>> = backingArray

    private var currentHighestPriority = Int.MAX_VALUE

    private var size = 0


    fun add(element: T, priority: Int) {
        currentHighestPriority = minOf(priority, currentHighestPriority)
        queuesByPriority[priority].add(element)
        size++
    }

    fun poll(): T? {
        if (size == 0) return null
        for (i in currentHighestPriority..queuesByPriority.size - 1) {
            currentHighestPriority = minOf(i, currentHighestPriority)
            val polled = queuesByPriority[i].poll()
            if (polled != null) {
                size--
                return polled
            }
        }
        return null
    }


    companion object {
        internal inline operator fun <reified T> invoke(prioritiesAmount: Int) =
            PriorityArrayQueue<T>(Array(prioritiesAmount) { ArrayQueue() })
    }

}