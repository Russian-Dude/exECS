package com.rdude.exECS.component.state

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.world.World

/** Manages [ObservableComponent.world].*/
internal class ObservableComponentStateManager(world: World) : ComponentStateManager<ObservableComponent<*>>(world) {

    override fun componentAdded(component: ObservableComponent<*>, entityId: Int) {
        component.world = world
    }

    override fun componentRemoved(component: ObservableComponent<*>, entityId: Int) {
        // do nothing
    }

    override fun componentChangedId(component: ObservableComponent<*>, from: Int, to: Int) {
        // do nothing
    }
}