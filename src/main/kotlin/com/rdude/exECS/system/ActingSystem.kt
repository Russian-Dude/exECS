package com.rdude.exECS.system

import com.rdude.exECS.event.ActingEvent
import com.rdude.exECS.world.World

/** System that is being triggered every [World.act] method execution.
 * @see System
 * @see EventSystem
 * @see IterableEventSystem
 * @see IterableActingSystem*/
abstract class ActingSystem : EventSystem<ActingEvent>() {

    final override fun eventFired(event: ActingEvent) = act()

    /** Implement this method to specify a behaviour when [ActingEvent] is fired.*/
    abstract fun act()

}