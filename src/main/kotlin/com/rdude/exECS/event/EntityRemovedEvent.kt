package com.rdude.exECS.event

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.world.World

class EntityRemovedEvent(world: World): PoolableEvent() {

    internal var pureEntity: Entity = Entity.DUMMY_ENTITY
        set(value) {
            field = value
            entity.entity = value
        }

    internal var entityId: EntityID = EntityID.DUMMY_ENTITY_ID
        set(value) {
            field = value
            entity.entityID = value
        }

    val entity: EntityWrapper = EntityWrapper(world)

}