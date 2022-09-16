package com.rdude.exECS.component.state

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.PoolableComponent
import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.decreaseCount
import com.rdude.exECS.utils.increaseCount
import com.rdude.exECS.world.World

/** This manager will be used for Poolable Components ([Component] AND [Poolable], not [PoolableComponent])
 * for those classes that were compiled **without** exECS compiler plugin.*/
internal class FakePoolableComponentStateManager(world: World) : ComponentStateManager<Component>(world) {

    @JvmField
    internal var autoReturnToPool: Boolean = ExEcsGlobalConfiguration.worldDefaultConfiguration.autoReturnPoolableComponentsToPool

    override fun componentAdded(component: Component, entityId: Int) {
        PoolableComponent.componentsToInsideEntitiesAmount.increaseCount(component)
    }

    override fun componentRemoved(component: Component, entityId: Int) {
        val insideEntities = PoolableComponent.componentsToInsideEntitiesAmount.decreaseCount(component)
        if (insideEntities == 0 && autoReturnToPool) {
            world.poolablesManager.poolableNeedsToBeReturnedToPool(component as Poolable)
        }
    }

    override fun componentChangedId(component: Component, from: Int, to: Int) {
        // do nothing
    }
}