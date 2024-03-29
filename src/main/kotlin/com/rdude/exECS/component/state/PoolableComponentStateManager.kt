package com.rdude.exECS.component.state

import com.rdude.exECS.component.PoolableComponent
import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.world.World

/** Manages [PoolableComponent.insideEntities] and auto-returning to Pool.*/
internal class PoolableComponentStateManager(world: World) : ComponentStateManager<PoolableComponent>(world) {

    @JvmField
    internal var autoReturnToPool: Boolean = ExEcsGlobalConfiguration.worldDefaultConfiguration.autoReturnPoolableComponentsToPool

    override fun componentAdded(component: PoolableComponent, entityId: Int) {
        component.insideEntities++
    }

    override fun componentRemoved(component: PoolableComponent, entityId: Int) {
        component.insideEntities--
        if (component.insideEntities == 0 && autoReturnToPool) {
            world.poolablesManager.poolableNeedsToBeReturnedToPool(component as Poolable)
        }
    }

    override fun componentChangedId(component: PoolableComponent, from: Int, to: Int) {
        // do nothing
    }
}