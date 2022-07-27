package com.rdude.exECS.utils.collections

/**Stores a pair of event type id to component type id as a single long value.*/
@JvmInline
internal value class EventTypeToComponentTypePair(val data: Long) {

    constructor(eventTypeId: Int, componentTypeId: Int) : this((eventTypeId.toLong() shl 32) + componentTypeId)

    inline fun eventId(): Int = (data shr 32).toInt()

    inline fun componentId(): Int = data.toInt()
}
