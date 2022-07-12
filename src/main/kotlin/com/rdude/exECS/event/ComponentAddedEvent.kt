package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.utils.Dummies
import com.rdude.exECS.system.System


/** Every time a [Component] of type [T] is added to an [Entity], if at least one [System] is subscribed to this Event
 * with type [T], this Event is queued.*/
class ComponentAddedEvent<T : Component> internal constructor() : InternalPoolableEvent(), ComponentRelatedEvent<T> {

    /** The [Entity] to which the [component] was added.*/
    var entity: Entity = Dummies.DUMMY_ENTITY_WRAPPER
        internal set

    /** The [Component] that was added to the [entity].*/
    override lateinit var component: T
        internal set

    /** [Component] of the same type [T] that was replaced by the [component] in the [entity].*/
    var replacedComponent: T? = null
        internal set

    /** True if the added [component] has replaced another one.*/
    val replaced get() = replacedComponent != null

    // hardcoded for performance
    override fun getEventTypeId(): Int = 0

    internal companion object {
        val pool = Pool { ComponentAddedEvent<Component>() }
    }
}