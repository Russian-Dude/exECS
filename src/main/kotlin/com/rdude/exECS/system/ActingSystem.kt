package com.rdude.exECS.system

import com.rdude.exECS.event.ActingEvent

abstract class ActingSystem : EventSystem<ActingEvent>() {

    final override fun eventFired(event: ActingEvent) = act(event.delta)

    abstract fun act(delta: Double)

}