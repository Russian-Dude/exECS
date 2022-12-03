package com.rdude.exECS.entity

fun interface EntityComparator {

    fun compare(e1: Entity, e2: Entity): Int

    companion object {
        val DO_NOT_COMPARE = EntityComparator { _, _ ->
            throw IllegalStateException("EntityComparator DO_NOT_COMPARE was used to perform an actual comparison")
        }
    }
}