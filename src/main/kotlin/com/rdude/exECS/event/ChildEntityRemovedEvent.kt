package com.rdude.exECS.event

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.EventSystem

/** Every time a child [Entity] is removed from the parent Entity, if at least one [EventSystem] is subscribed to this
 * Event, this Event is queued.*/
class ChildEntityRemovedEvent : InternalPoolableEvent() {

    var childEntity: Entity = Entity.NO_ENTITY
        internal set

    var parentEntity: Entity = Entity.NO_ENTITY
        internal set

    override fun getEventTypeId(): Int = EventTypeIDsResolver.CHILD_ENTITY_REMOVED_ID

    internal companion object {
        val pool = Pool { ChildEntityRemovedEvent() }
    }
}