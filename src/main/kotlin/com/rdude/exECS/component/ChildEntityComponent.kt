package com.rdude.exECS.component

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool

/** [Entity] that has a parent will have this component.*/
internal class ChildEntityComponent internal constructor() : UniqueComponent, PoolableComponent {

    @JvmField
    @PublishedApi
    internal var parentEntityId: Int = Entity.NO_ENTITY.id

    inline val parent: Entity get() = Entity(parentEntityId)

    override fun reset() {
        parentEntityId = Entity.NO_ENTITY.id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChildEntityComponent
        if (parentEntityId != other.parentEntityId) return false
        return true
    }

    override fun hashCode(): Int {
        return parentEntityId
    }


    internal companion object {
        @JvmField
        internal val pool = Pool { ChildEntityComponent() }
    }
}