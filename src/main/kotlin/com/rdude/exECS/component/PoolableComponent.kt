package com.rdude.exECS.component

import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import java.util.*
import kotlin.math.max

/** Combined [Component] and [Poolable] interfaces.
 * Poolable [Component] will be automatically returned to the [Pool] whenever it is removed from an Entity and there are no other
 * Entities containing this [Component]. For this purpose it should know to how many entities it is currently plugged
 * into. This information is stored as well for [Component]s that implements only [Poolable] interface but not PoolableComponent.
 * ```
 * entity.addComponent<PositionComponent>() // add component to the entity from the default pool
 * entity.addComponent<PositionComponent> { x = 5; y = 0 } // add customized component to the entity from the default pool
 * ```
 * @see [Pool]*/
interface PoolableComponent : Component, Poolable {

    /** Amount of Entities that contains [Component] instance.
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Simple-properties-overriding) property at compile time.*/
    var insideEntities: Int
        get() = componentsToInsideEntitiesAmount[this] ?: 0
        set(value) { componentsToInsideEntitiesAmount[this] = max(value, 0) }


    companion object {

        /** This map is used to store amount of Entities that contains [Component] instance
         * only for those PoolableComponent (or [Component] and [Poolable]) subclasses that were compiled without exECS plugin.*/
        internal val componentsToInsideEntitiesAmount: MutableMap<Component, Int> = IdentityHashMap()

    }

}