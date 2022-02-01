package com.rdude.exECS.event

import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.EventSystemGenericQualifier
import com.rdude.exECS.utils.collections.IterableList
import java.util.*
import kotlin.reflect.KClass

class EventBus {

    private val eventQueue: Queue<Event> = LinkedList()
    private val eventsToSystems: MutableMap<KClass<out Event>, IterableList<EventSystem<*>>> = HashMap()

    fun registerSystem(system: EventSystem<*>) {
        for (eventClass in EventSystemGenericQualifier.getEventClassesForSystem(system)) {
            var iterableList = eventsToSystems[eventClass]
            if (iterableList == null) {
                iterableList = IterableList()
                eventsToSystems[eventClass] = iterableList
            }
            iterableList.add(system)
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
            val iterableList = eventsToSystems[event::class]
            if (iterableList != null) {
                for (system in iterableList as IterableList<EventSystem<in Event>>) {
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