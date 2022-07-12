package com.rdude.exECS.event

import com.rdude.exECS.world.World
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.system.SimpleActingSystem

/** Main [Event] of the exECS. Queued at every [World.act] method execution.
 * @see ActingSystem
 * @see SimpleActingSystem*/
class ActingEvent(var delta: Double) : InternalEvent() {

    // hardcoded for performance
    override fun getEventTypeId(): Int = 3

}
