package com.rdude.exECS.config

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.ActingEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.event.EventPriority
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.world.World


class WorldConfiguration internal constructor() {


    /** If true, [Components][Component] implementing [Poolable] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    @JvmField
    var autoReturnPoolableComponentsToPool: Boolean = ExEcsGlobalConfiguration.autoReturnPoolableComponentsToPool


    /** If true, [Events][Event] implementing [Poolable] will be automatically returned to the [Pool] as soon
     * as they are fired.*/
    @JvmField
    var autoReturnPoolableEventsToPool: Boolean = ExEcsGlobalConfiguration.autoReturnPoolableEventsToPool


    /** [EventPriority] of [ActingEvent].*/
    @JvmField
    var actingEventPriority: EventPriority = ExEcsGlobalConfiguration.actingEventPriority


    /** Sets all properties to be the same as the corresponding properties in [ExEcsGlobalConfiguration].*/
    fun setAllFromGlobalConfiguration() {
        autoReturnPoolableComponentsToPool = ExEcsGlobalConfiguration.autoReturnPoolableComponentsToPool
        autoReturnPoolableEventsToPool = ExEcsGlobalConfiguration.autoReturnPoolableEventsToPool
        actingEventPriority = ExEcsGlobalConfiguration.actingEventPriority
    }

}