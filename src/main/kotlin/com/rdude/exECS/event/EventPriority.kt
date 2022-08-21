package com.rdude.exECS.event

/** [Events][Event] with higher priority will be fired first.*/
enum class EventPriority(@JvmField internal val value: Int) {

    CRITICAL(0),
    HIGH(1),
    MEDIUM(2),
    LOW(3)

}
