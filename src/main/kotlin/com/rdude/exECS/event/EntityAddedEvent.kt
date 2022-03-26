package com.rdude.exECS.event

import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.world.World

class EntityAddedEvent(world: World): InternalPoolableEvent() {

    internal var entityId: Int = 0
        set(value) {
            field = value
            entity.entityID = value
        }

    val entity: EntityWrapper = EntityWrapper(world)

}