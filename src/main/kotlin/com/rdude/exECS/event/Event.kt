package com.rdude.exECS.event

interface Event {

    fun getTypeId(): Int = EventTypeIDsResolver.idFor(this::class)

}