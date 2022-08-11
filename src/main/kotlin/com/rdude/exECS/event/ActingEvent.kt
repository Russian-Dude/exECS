package com.rdude.exECS.event

import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World

/** Main [Event] of the exECS. Queued at every [World.act] method execution.
 * @see IterableActingSystem
 * @see ActingSystem*/
object ActingEvent : InternalEvent() {

    // hardcoded for performance
    override fun getEventTypeId(): Int = 0

}
