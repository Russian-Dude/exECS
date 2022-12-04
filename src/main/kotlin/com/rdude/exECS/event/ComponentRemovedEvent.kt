package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.ExEcs

/** Every time a [Component] of type [T] is removed from an [Entity], if at least one [EventSystem] is subscribed to this Event
 * with type [T], this event is queued.*/
class ComponentRemovedEvent<T : Component> internal constructor() : InternalPoolableEvent(), ComponentRelatedEvent<T> {

    /** The [Entity] from which the [component] was removed.*/
    var entity: Entity = Entity.NO_ENTITY
        internal set

    /** The [Component] that was removed from the [entity].*/
    override lateinit var component: T
        internal set

    override fun getEventTypeId(): Int =
        EventTypeIDsResolver.INTERNAL_NON_COMPONENT_RELATED_EVENTS_AMOUNT +
                ExEcs.componentTypeIDsResolver.size +
                component.getComponentTypeId()


    internal companion object {
        val pool = Pool { ComponentRemovedEvent<Component>() }
    }
}