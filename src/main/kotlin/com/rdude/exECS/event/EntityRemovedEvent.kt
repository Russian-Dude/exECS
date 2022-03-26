package com.rdude.exECS.event

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.utils.Dummies
import com.rdude.exECS.world.World

class EntityRemovedEvent(world: World): InternalPoolableEvent() {

    internal var entityId: Int = Dummies.DUMMY_ENTITY_ID
        set(value) {
            field = value
            entity.entityID = value
        }

    val entity: EntityWrapper = EntityWrapper(world)

}