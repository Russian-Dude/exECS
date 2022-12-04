package com.rdude.exECS.event

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.EventSystem

/** Every time an [Entity] is added to another Entity as a child, if at least one [EventSystem] is subscribed to
 * this Event, this Event is queued.*/
class ChildEntityAddedEvent : InternalPoolableEvent() {

    var childEntity: Entity = Entity.NO_ENTITY
        internal set

    var parentEntity: Entity = Entity.NO_ENTITY
        internal set

    override fun getEventTypeId(): Int = EventTypeIDsResolver.CHILD_ENTITY_ADDED_ID


    internal companion object {
        val pool = Pool { ChildEntityAddedEvent() }
    }
}