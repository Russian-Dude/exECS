package com.rdude.exECS.event

import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.EventSystemGenericQualifier
import com.rdude.exECS.utils.collections.IterableArray
import java.util.*
import kotlin.reflect.KClass

class EventBus {

    private val eventQueue: Queue<Event> = LinkedList()
    private val eventsToSystems: MutableMap<KClass<out Event>, IterableArray<EventSystem<*>>> = HashMap()

    fun registerSystem(system: EventSystem<*>) {
        for (eventClass in EventSystemGenericQualifier.getEventClassesForSystem(system)) {
            var iterableArray = eventsToSystems[eventClass]
            if (iterableArray == null) {
                iterableArray = IterableArray()
                eventsToSystems[eventClass] = iterableArray
            }
            iterableArray.add(system)
        }
    }

    fun removeSystem(system: EventSystem<*>) {
        for (eventClass in EventSystemGenericQualifier.getEventClassesForSystem(system)) {
            eventsToSystems[eventClass]?.remove(system)
        }
    }

    fun queueEvent(event: Event) = eventQueue.add(event)

    @Suppress("UNCHECKED_CAST")
    fun fireEvents() {
        var event = eventQueue.poll()
        while (event != null) {
            val iterableArray = eventsToSystems[event::class]
            if (iterableArray != null) {
                for (system in iterableArray as IterableArray<EventSystem<in Event>>) {
                    system.fireEvent(event)
                }
            }
            if (event is PoolableEvent) {
                event.returnToPool()
            }
            event = eventQueue.poll()
        }
    }

}