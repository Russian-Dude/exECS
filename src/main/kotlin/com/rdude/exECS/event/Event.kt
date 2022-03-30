package com.rdude.exECS.event

interface Event {

    fun getEventTypeId(): Int = EventTypeIDsResolver.idFor(this::class)

}