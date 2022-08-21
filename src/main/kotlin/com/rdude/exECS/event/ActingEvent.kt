package com.rdude.exECS.event

import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World

/** Main [Event] of the exECS. Queued at every [World.act] method execution.
 * @see IterableActingSystem
 * @see ActingSystem*/
object ActingEvent : InternalEvent() {

    /** Every [World] can use its own custom priority for [ActingEvent], specified in [World.configuration],
     * which will be used when ActingEvent is queued while executing the [World.act] method.
     * This method is overridden for situations when [ActingEvent] is queued outside the [World.act] method. In this
     * case, priority from the [ExEcsGlobalConfiguration.actingEventPriority] will be used.*/
    override fun defaultPriority(): EventPriority = ExEcsGlobalConfiguration.actingEventPriority

    // hardcoded for performance
    override fun getEventTypeId(): Int = 0

}
