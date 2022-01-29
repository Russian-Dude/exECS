package com.rdude.exECS.utils.collections

/**
 * THIS LIST WAS CREATED WITH INTENTION TO BE USED ONLY BY THIS ECS LIBRARY.
 *
 * List with reusable iterator. Only one iteration per time is available.
 * When using for loops with this list, break MUST NOT BE CALLED, instead forceBreak method should be used
 */

class IterableList<T> {

    private val iterator: ReusableIterator by lazy { ReusableIterator() }
    private var currentlyIterating = false

    private var first: Node = Node(null, null)
    private var last: Node = first

    constructor(vararg elements: T) {
        addAll(*elements)
    }

    fun contains(element: T): Boolean {
        var node: Node? = first
        while (node != null) {
            if (node.element == element) {
                return true
            }
            node = node.next
        }
        return false
    }

    operator fun iterator(): Iterator<T> {
        if (currentlyIterating) throw IllegalStateException("Nested iterations are not allowed")
        iterator.reset()
        return iterator
    }

    fun add(element: T) {
        val node = Node(element, null)
        last.next = node
        last = node
    }

    fun addAll(vararg elements: T) {
        for (i in 0..elements.size - 1) {
            add(elements[i])
        }
    }

    fun remove(element: T) {
        var prev: Node = first
        var node: Node? = first
        while (node != null) {
            if (node.element == element) {
                if (last == node) {
                    last = prev
                }
                node.element = null
                prev.next = node.next
            }
            prev = node
            node = node.next
        }
    }

    fun clear() {
        first.element = null
        first.next = null
        last = first
    }

    fun isNotEmpty() : Boolean = first.element != null || first.next != null

    fun isEmpty() : Boolean = !isNotEmpty()

    fun forceBreak() {
        iterator.current = last
    }

    operator fun plusAssign(element: T) = add(element)

    operator fun minusAssign(element: T) = remove(element)


    private inner class Node(var element: T?, var next: Node?)


    private inner class ReusableIterator : Iterator<T> {

        var current: Node = first

        fun reset() {
            current = first
        }

        override fun hasNext(): Boolean {
            val hasNext = current.next != null
            currentlyIterating = hasNext
            return hasNext
        }

        override fun next(): T {
            current = current.next ?: throw NullPointerException()
            return current.element ?: throw NullPointerException()
        }
    }
}