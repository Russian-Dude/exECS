package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.EventSystem


/** Every time a [Component] of type [T] is added to an [Entity], if at least one [EventSystem] is subscribed to this Event
 * with type [T], this Event is queued.*/
class ComponentAddedEvent<T : Component> internal constructor() : InternalPoolableEvent(), ComponentRelatedEvent<T> {

    /** The [Entity] to which the [component] was added.*/
    var entity: Entity = Entity.NO_ENTITY
        internal set

    /** The [Component] that was added to the [entity].*/
    override lateinit var component: T
        internal set

    /** [Component] of the same type [T] that was replaced by the [component] in the [entity].*/
    var replacedComponent: T? = null
        internal set

    /** True if the added [component] has replaced another one.*/
    val replaced get() = replacedComponent != null

    // notComponentRelatedInternalEventsAmount + (eventId * componentsAmount) + componentId
    override fun getEventTypeId(): Int =
        EventTypeIDsResolver.INTERNAL_NON_COMPONENT_RELATED_EVENTS_AMOUNT + component.getComponentTypeId()

    internal companion object {
        val pool = Pool { ComponentAddedEvent<Component>() }
    }
}