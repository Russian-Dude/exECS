package com.rdude.exECS.system

import com.rdude.exECS.event.Event

abstract class EventSystem<T : Event> : System() {

    var enabled = true

    protected abstract fun eventFired(event: T)

    internal fun fireEvent(event: T) = eventFired(event)

}