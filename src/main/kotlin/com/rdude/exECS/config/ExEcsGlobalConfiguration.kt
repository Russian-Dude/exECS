package com.rdude.exECS.config

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.ActingEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.EventPriority
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.world.World

object ExEcsGlobalConfiguration {

    /** New [World] instances will set their properties as specified in this property.*/
    @JvmField
    val worldDefaultConfiguration = WorldDefaultConfiguration()


    /** Describes what happens when [Poolable.returnToPool] is called and [Poolable.pool] is null.*/
    @JvmField
    var onPoolIsNotSet: OnPoolIsNotSet = OnPoolIsNotSet.THROW


    /** Describes what happens when [Poolable.returnToPool] is called but [Poolable] is already in [Pool] ([Poolable.isInPool] is true).*/
    @JvmField
    var onAlreadyInPool: OnAlreadyInPool = OnAlreadyInPool.THROW


    /** New [World] instances will set their properties as specified in this class.*/
    class WorldDefaultConfiguration {

        /** If true, [Components][Component] implementing [Poolable] will be automatically returned to the [Pool] as soon
         * as they are not plugged into any [Entity].
         *
         * New [World] instances will set their [WorldConfiguration.setAutoReturnPoolableComponentsToPool] to this value.
         *
         * Changes of this property will **not** affect [WorldConfiguration.setAutoReturnPoolableComponentsToPool] of
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


        /** [EventPriority] of [ActingEvent].
         *
         * New [World] instances will set their [WorldConfiguration.actingEventPriority] property to this value.
         *
         * Changes of this property will **not** affect [WorldConfiguration.actingEventPriority] properties of
         * existing [Worlds][World].*/
        @JvmField
        var actingEventPriority: EventPriority = EventPriority.HIGH


        /** If true, [ComponentAddedEvent] will be queued for every [Component] of added [Entity].
         *
         * New [World] instances will set their [WorldConfiguration.queueComponentAddedWhenEntityAdded] property to this value.
         *
         * Changes of this property will **not** affect [WorldConfiguration.queueComponentAddedWhenEntityAdded] properties of
         * existing [Worlds][World].*/
        @JvmField
        var queueComponentAddedWhenEntityAdded: Boolean = false


        /** If true, [ComponentRemovedEvent] will be queued for every [Component] of removed [Entity].
         *
         * New [World] instances will set their [WorldConfiguration.queueComponentRemovedWhenEntityRemoved] property to this value.
         *
         * Changes of this property will **not** affect [WorldConfiguration.queueComponentRemovedWhenEntityRemoved] properties of
         * existing [Worlds][World].*/
        @JvmField
        var queueComponentRemovedWhenEntityRemoved: Boolean = false

    }
}