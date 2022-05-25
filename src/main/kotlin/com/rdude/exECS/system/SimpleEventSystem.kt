package com.rdude.exECS.system

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.Event

abstract class SimpleEventSystem<T: Event> : EventSystem<T>() {

    abstract fun eventFired(event: T)

    final override fun eventFired(entity: EntityWrapper, event: T) = eventFired(event)

    final override fun startActing() {}

    final override fun endActing() {}

}