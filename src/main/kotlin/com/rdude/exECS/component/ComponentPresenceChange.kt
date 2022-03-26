package com.rdude.exECS.component

import kotlin.math.abs

/**
 * Stores data about component needs to be added or removed to an entity.
 * Data stored as a single long value that represents two int values: entityID and componentID.
 * If stored long value is negative - component needs to be removed, if value is positive - component needs to be added.
 * Constructing new data from integers is unsafe because there are no checks for IDs to be positive values.
 */
@JvmInline
internal value class ComponentPresenceChange(val data: Long) {

    constructor(entityID: Int, componentId: Int, removed: Boolean)
            : this(((entityID.toLong() shl 32) + componentId) * if (removed) -1 else 1)

    inline fun entityId(): Int = (abs(data) shr 32).toInt()

    inline fun componentId(): Int = abs(data).toInt()

    inline fun isRemoved(): Boolean = data < 0

}



