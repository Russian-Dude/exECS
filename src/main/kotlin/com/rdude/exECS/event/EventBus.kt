package com.rdude.exECS.event

import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.collections.ArrayQueue
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.reflection.ReflectionUtils
import kotlin.reflect.KClass

class EventBus {

    // Main event queue
    private val eventQueue = ArrayQueue.create<Event>()

    // Component added/removed and entity/added removed events queue
    private val internalEventQueue = ArrayQueue.create<Event>()

    // System subscriptions to events
    private val eventsToSystems: MutableMap<KClass<out Event>, IterableArray<EventSystem<*>>> = HashMap()

    fun registerSystem(system: EventSystem<*>) {
        for (eventClass in ReflectionUtils.eventSystemGenericQualifier.getEventClassesForSystem(system)) {
            var iterableArray = eventsToSystems[eventClass]
            if (iterableArray == null) {
                iterableArray = IterableArray()
                eventsToSystems[eventClass] = iterableArray
            }
            iterableArray.add(system)
        }
    }

    fun removeSystem(system: EventSystem<*>) {
        for (eventClass in ReflectionUtils.eventSystemGenericQualifier.getEventClassesForSystem(system)) {
            eventsToSystems[eventClass]?.remove(system)
        }
    }

    fun queueEvent(event: Event) = eventQueue.add(event)

    internal fun queueInternalEvent(event: Event) = internalEventQueue.add(event)

    @Suppress("UNCHECKED_CAST")
    internal fun fireEvents() {
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

    @Suppress("UNCHECKED_CAST")
    internal fun fireInternalEvents() {
        var event = internalEventQueue.poll()
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
            event = internalEventQueue.poll()
        }
    }

}