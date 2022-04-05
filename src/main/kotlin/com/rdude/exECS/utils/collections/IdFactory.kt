package com.rdude.exECS.utils.collections

class IdFactory {

    private var current = 0
    private val reuse = IntArrayStack()

    fun obtain(): Int = if (reuse.isEmpty()) current++ else reuse.unsafePoll()

    fun free(id: Int) = reuse.add(id)

}