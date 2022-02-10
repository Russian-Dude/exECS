package com.rdude.exECS.component

import com.rdude.exECS.entity.EntityID
import kotlin.math.abs

/**
 * Stores data about component needs to be added or removed to an entity.
 * Data stored as a single long value that represents two int values: entityID and componentID.
 * If stored long value is negative - component needs to be removed, if value is positive - component needs to be added.
 * Constructing new data from integers is unsafe because there are no checks for IDs to be positive values.
 */
@JvmInline
internal value class ComponentPresenceChange(val data: Long) {

    constructor(entityID: EntityID, componentId: ComponentTypeID, removed: Boolean)
            : this(((entityID.id.toLong() shl 32) + componentId.id) * if (removed) -1 else 1)

    inline fun entityId(): EntityID = EntityID((abs(data) shr 32).toInt())

    inline fun componentId(): ComponentTypeID = ComponentTypeID(abs(data).toInt())

    inline fun isRemoved(): Boolean = data < 0

}



