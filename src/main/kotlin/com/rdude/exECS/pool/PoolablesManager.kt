package com.rdude.exECS.pool

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.PoolableComponent
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.world.World

/** Managed [Poolable]s that needs to be returned to [Pool] at the end of the [World.act] call.*/
internal class PoolablesManager {

    private val poolablesToReturn = IterableArray<Poolable>()

    fun poolableNeedsToBeReturnedToPool(poolable: Poolable) = poolablesToReturn.add(poolable)

    // Currently, manages only Components, because Events returns to pool immediately after being fired by an event bus
    fun returnPoolablesToPoolIfNeeded() {
        poolablesToReturn.removeIf {
            if (it is Component && ((it as? PoolableComponent)?.insideEntities ?: PoolableComponent.componentsToInsideEntitiesAmount[it]) == 0) {
                it.returnToPool()
                true
            }
            else false
        }
    }

}