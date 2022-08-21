package com.rdude.exECS.config

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.Event
import com.rdude.exECS.event.EventPriority
import com.rdude.exECS.event.ActingEvent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.world.World

object ExEcsGlobalConfiguration {

    /** If true, [Components][Component] implementing [Poolable] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].
     *
     * New [World] instances will set their [WorldConfiguration.autoReturnPoolableComponentsToPool] property to this value.
     *
     * Changes of this property will **not** affect [WorldConfiguration.autoReturnPoolableComponentsToPool] properties of
     * existing [Worlds][World].*/
    @JvmField
    var autoReturnPoolableComponentsToPool: Boolean = true


    /** If true, [Events][Event] implementing [Poolable] will be automatically returned to the [Pool] as soon
     * as they are fired.
     *
     * New [World] instances will set their [WorldConfiguration.autoReturnPoolableEventsToPool] property to this value.
     *
     * Changes of this property will **not** affect [WorldConfiguration.autoReturnPoolableEventsToPool] properties of
     * existing [Worlds][World].*/
    @JvmField
    var autoReturnPoolableEventsToPool: Boolean = true


    /** Describes what happens when [Poolable.returnToPool] is called and [Poolable.pool] is null.*/
    @JvmField
    var onPoolIsNotSet: OnPoolIsNotSet = OnPoolIsNotSet.THROW


    /** [EventPriority] of [ActingEvent].
     *
     * New [World] instances will set their [WorldConfiguration.actingEventPriority] property to this value.
     *
     * Changes of this property will **not** affect [WorldConfiguration.actingEventPriority] properties of
     * existing [Worlds][World].*/
    @JvmField
    var actingEventPriority: EventPriority = EventPriority.HIGH

}