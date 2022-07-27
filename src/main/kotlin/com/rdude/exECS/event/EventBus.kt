package com.rdude.exECS.event

import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ArrayQueue
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.eventsTypesSubscription
import com.rdude.exECS.world.World

internal class EventBus(private val world: World) {

    private val eventQueue = ArrayQueue<Event>()

    private val eventsToSystems: Array<IterableArray<EventSystem<*>>> = Array(ExEcs.eventTypeIDsResolver.size) { IterableArray() }


    fun registerSystem(system: EventSystem<*>) {
        system.eventsTypesSubscription.eventIds.forEach {
            eventsToSystems[it].add(system)
        }
    }

    fun removeSystem(system: EventSystem<*>) {
        system.eventsTypesSubscription.eventIds.forEach {
            eventsToSystems[it].remove(system)
        }
    }

    fun queueEvent(event: Event) = eventQueue.add(event)

    @Suppress("UNCHECKED_CAST")
    internal fun fireEvents() {
        var event = eventQueue.poll()
        while (event != null) {
            (eventsToSystems[event.getEventTypeId()]).forEach {
                world.updateSubscriptions()
                if (it.enabled) {
                    it as EventSystem<in Event>
                    it.fireEvent(event!!)
                }
            }
            if (event is Poolable) {
                event.returnToPool()
            }
            event = eventQueue.poll()
        }
    }

}