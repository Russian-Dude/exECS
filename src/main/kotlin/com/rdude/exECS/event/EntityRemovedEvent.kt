package com.rdude.exECS.event

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World

/** Every time an [Entity] is removed from the [World], if at least one [EventSystem] is subscribed to this Event, this Event is queued.*/
class EntityRemovedEvent internal constructor() : InternalPoolableEvent() {

    /** The [Entity] that was removed from the [World].*/
    var entity: Entity = Entity.NO_ENTITY
        internal set

    override fun getEventTypeId(): Int = EventTypeIDsResolver.ENTITY_REMOVED_ID


    internal companion object {
        val pool = Pool { EntityRemovedEvent() }
    }

}