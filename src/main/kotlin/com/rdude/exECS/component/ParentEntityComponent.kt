package com.rdude.exECS.component

import com.rdude.exECS.pool.Pool
import com.rdude.exECS.utils.collections.EntitiesSet
import com.rdude.exECS.entity.Entity

/** [Entity] that has children will have this component.*/
internal class ParentEntityComponent internal constructor() : UniqueComponent, PoolableComponent {

    @JvmField
    val children = EntitiesSet()

    override fun reset() {
        children.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ParentEntityComponent
        if (children != other.children) return false
        return true
    }

    override fun hashCode(): Int {
        return children.hashCode()
    }


    internal companion object {
        @JvmField
        internal val pool = Pool { ParentEntityComponent() }
    }

}