package com.rdude.exECS.component.state

import com.rdude.exECS.component.RichComponent
import com.rdude.exECS.world.World

/** Manages [RichComponent.insideEntitiesSet].*/
internal class RichComponentStateManager(world: World) : ComponentStateManager<RichComponent>(world) {

    override fun componentAdded(component: RichComponent, entityId: Int) {
        component.insideEntitiesSet.add(entityId)
    }

    override fun componentRemoved(component: RichComponent, entityId: Int) {
        component.insideEntitiesSet.remove(entityId)
    }

    override fun componentChangedId(component: RichComponent, from: Int, to: Int) {
        component.insideEntitiesSet.replace(from, to)
    }
}