package com.rdude.exECS.system

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.Event

abstract class SimpleEventSystem<T: Event> : EventSystem<T>() {

    abstract fun eventFired(event: T)

    final override fun eventFired(entity: Entity, event: T) = eventFired(event)

}