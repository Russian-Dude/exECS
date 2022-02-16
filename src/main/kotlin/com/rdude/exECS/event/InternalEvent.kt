package com.rdude.exECS.event

abstract class InternalEvent : Event {
    internal val id: EventTypeID = EventTypeIDsResolver.idFor(this::class)
}