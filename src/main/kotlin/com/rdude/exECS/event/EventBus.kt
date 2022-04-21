package com.rdude.exECS.event

import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ArrayQueue
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.reflection.ReflectionUtils

class EventBus(mainEvent: ActingEvent) {

    // Main event
    private val actingEvent = mainEvent

    // Main event queue
    internal val eventQueue = ArrayQueue<Event>()

    // Component added/removed and entity/added removed events queue
    private val internalEventQueue = ArrayQueue<InternalEvent>()

    // Systems subscribed to the main acting event. Stored independently of other events for performance reasons.
    private val actingEventSubscribers = IterableArray<EventSystem<ActingEvent>>()

    // System subscriptions to events
    private val eventsToSystems: Array<IterableArray<EventSystem<*>>> = Array(ExEcs.eventTypeIDsResolver.size) { IterableArray() }

    @Suppress("UNCHECKED_CAST")
    fun registerSystem(system: EventSystem<*>) {
        for (eventClass in ExEcs.eventSystemGenericQualifier.getEventClassesForSystem(system)) {
            if (eventClass == ActingEvent::class) {
                actingEventSubscribers.add(system as EventSystem<ActingEvent>)
            }
            else {
                eventsToSystems[ExEcs.eventTypeIDsResolver.idFor(eventClass)].add(system)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun removeSystem(system: EventSystem<*>) {
        for (eventClass in ExEcs.eventSystemGenericQualifier.getEventClassesForSystem(system)) {
            if (eventClass == ActingEvent::class) {
                actingEventSubscribers.removeContainingOrder(system as EventSystem<ActingEvent>)
            }
            else {
                eventsToSystems[ExEcs.eventTypeIDsResolver.idFor(eventClass)].removeContainingOrder(system)
            }
        }
    }

    fun queueEvent(event: Event) = eventQueue.add(event)

    internal fun queueInternalEvent(event: InternalEvent) = internalEventQueue.add(event)

    @Suppress("UNCHECKED_CAST")
    internal fun fireEvents() {
        // fire main event
        for (system in actingEventSubscribers) {
            system.fireEvent(actingEvent)
        }
        // fire other queued events
        var event = eventQueue.poll()
        while (event != null) {
            val iterableArray = eventsToSystems[event.getEventTypeId()]
            for (system in iterableArray as IterableArray<EventSystem<in Event>>) {
                system.fireEvent(event)
            }
            if (event is Poolable) {
                event.returnToPool()
            }
            event = eventQueue.poll()
        }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun fireInternalEvents() {
        var event = internalEventQueue.poll()
        while (event != null) {
            val iterableArray = eventsToSystems[event.getEventTypeId()]
            for (system in iterableArray as IterableArray<EventSystem<in Event>>) {
                if (system.enabled) system.fireEvent(event)
            }
            if (event is Poolable) {
                event.returnToPool()
            }
            event = internalEventQueue.poll()
        }
    }

}