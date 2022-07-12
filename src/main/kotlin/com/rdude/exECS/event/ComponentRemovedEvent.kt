package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.Dummies

/** Every time a [Component] of type [T] is removed from an [Entity], if at least one [System] is subscribed to this Event
 * with type [T], this event is queued.*/
class ComponentRemovedEvent<T : Component> internal constructor() : InternalPoolableEvent(), ComponentRelatedEvent<T> {

    /** The [Entity] from which the [component] was removed.*/
    var entity: Entity = Dummies.DUMMY_ENTITY_WRAPPER
        internal set

    /** The [Component] that was removed from the [entity].*/
    override lateinit var component: T
        internal set

    // hardcoded for performance
    override fun getEventTypeId(): Int = 1


    internal companion object {
        val pool = Pool { ComponentRemovedEvent<Component>() }
    }
}