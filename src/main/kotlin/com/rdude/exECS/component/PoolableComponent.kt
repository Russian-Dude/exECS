package com.rdude.exECS.component

import com.rdude.exECS.pool.Poolable
import java.util.*
import kotlin.math.max

interface PoolableComponent : Component, Poolable {

    /** Amount of Entities that contains Component instance.
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by generated property at compile time.*/
    var insideEntities: Int
        get() = componentsToInsideEntitiesAmount[this] ?: 0
        set(value) { componentsToInsideEntitiesAmount[this] = max(value, 0) }


    companion object {

        /** This map is used to store amount of Entities that contains Component instance
         * only for those PoolableComponent (or Component and Poolable) subclasses that were compiled without exECS plugin.*/
        internal val componentsToInsideEntitiesAmount: MutableMap<Component, Int> = IdentityHashMap()

    }

}