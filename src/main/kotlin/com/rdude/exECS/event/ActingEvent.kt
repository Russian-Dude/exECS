package com.rdude.exECS.event

import com.rdude.exECS.world.World
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.system.ActingSystem

/** Main [Event] of the exECS. Queued at every [World.act] method execution.
 * @see IterableActingSystem
 * @see ActingSystem*/
class ActingEvent(var delta: Double) : InternalEvent() {

    // hardcoded for performance
    override fun getEventTypeId(): Int = 0

}
