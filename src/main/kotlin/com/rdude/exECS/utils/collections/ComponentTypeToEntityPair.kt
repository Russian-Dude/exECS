package com.rdude.exECS.utils.collections

/**Stores a pair of component type id to entity id as a single long value.*/
@JvmInline
internal value class ComponentTypeToEntityPair(val data: Long) {

    inline fun entityId(): Int = (data shr 32).toInt()

    inline fun componentId(): Int = data.toInt()

    internal companion object {

        inline operator fun invoke(entityID: Int, componentTypeId: Int) =
            ComponentTypeToEntityPair((entityID.toLong() shl 32) + componentTypeId)

    }
}



