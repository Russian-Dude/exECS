package com.rdude.exECS.event

class ActingEvent(var delta: Double) : InternalEvent() {

    // hardcoded for performance
    override fun getTypeId(): Int = 0

}
