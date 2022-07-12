package com.rdude.exECS.utils.collections

/**Stores a pair of component type id to entity id as a single long value.*/
@JvmInline
internal value class ComponentTypeToEntityPair(val data: Long) {

    constructor(entityID: Int, componentTypeId: Int) : this((entityID.toLong() shl 32) + componentTypeId)

    inline fun entityId(): Int = (data shr 32).toInt()

    inline fun componentId(): Int = data.toInt()
}



