package com.rdude.exECS.component.state

import com.rdude.exECS.component.UniqueComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.exception.ComponentStateException
import com.rdude.exECS.world.World

/** Manages [UniqueComponent.entityId].*/
internal class UniqueComponentStateManager(world: World) : ComponentStateManager<UniqueComponent>(world) {

    override fun componentAdded(component: UniqueComponent, entityId: Int) {
        if (component.entityId != Entity.NO_ENTITY.id) {
            throw ComponentStateException("Can not add Component of ${component::class} to an Entity. " +
                    "This Component is UniqueComponent and is already plugged into another Entity.")
        }
        component.entityId = entityId
    }

    override fun componentRemoved(component: UniqueComponent, entityId: Int) {
        component.entityId = Entity.NO_ENTITY.id
    }

    override fun componentChangedId(component: UniqueComponent, from: Int, to: Int) {
        component.entityId = to
    }
}