package com.rdude.exECS.event

import com.rdude.exECS.utils.ExEcs

interface Event {

    fun getEventTypeId(): Int = ExEcs.eventTypeIDsResolver.idFor(this::class)

}