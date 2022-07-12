package com.rdude.exECS.event

import com.rdude.exECS.aspect.ComponentRelatedEventSubscription
import com.rdude.exECS.aspect.EventSubscription
import com.rdude.exECS.aspect.SimpleEventSubscription
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.changeEach
import com.rdude.exECS.utils.collections.ArrayQueue
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.utils.eventTypeId
import com.rdude.exECS.world.World
import kotlin.reflect.full.isSubclassOf

internal class EventBus(private val world: World) {

    private val eventQueue = ArrayQueue<Event>()

    /** System subscriptions to component related events.*/
    private val componentRelatedEventsSubscribers = Array(3) { Array(ExEcs.componentTypeIDsResolver.size) { IterableArray<EventSystem<ComponentRelatedEvent<*>>>() } }

    /** System subscriptions to events.*/
    private val eventsToSystems: Array<IterableArray<EventSystem<*>>> = Array(ExEcs.eventTypeIDsResolver.size) { IterableArray() }

    /** How many systems are interested in specific internal events. Used to avoid firing those events if no one is interested.*/
    private val internalEventsInterests = InternalEventsInterests()


    @Suppress("UNCHECKED_CAST")
    fun registerSystem(system: EventSystem<*>) {
        val eventSubscriptions = ExEcs.eventSystemGenericQualifier.getEventSubscriptionsForSystem(system)
        for (eventSubscription in eventSubscriptions) {
            when (eventSubscription) {
                is SimpleEventSubscription -> {
                    eventsToSystems[eventSubscription.eventType.eventTypeId].add(system)
                }
                is ComponentRelatedEventSubscription -> {
                    componentRelatedEventsSubscribers[eventSubscription.eventType.eventTypeId][eventSubscription.componentType.componentTypeId]
                        .add(system as EventSystem<ComponentRelatedEvent<*>>)
                }
                else -> throw NotImplementedError("Can not register $system: registering event subscription type of ${eventSubscription::class} is not implemented in event bus")
            }
        }
        configInternalEventsProducers(eventSubscriptions, added = true)
    }

    @Suppress("UNCHECKED_CAST")
    fun removeSystem(system: EventSystem<*>) {
        val eventSubscriptions = ExEcs.eventSystemGenericQualifier.getEventSubscriptionsForSystem(system)
        for (eventSubscription in eventSubscriptions) {
            when (eventSubscription) {
                is SimpleEventSubscription -> {
                    eventsToSystems[eventSubscription.eventType.eventTypeId].removeContainingOrder(system)
                }
                is ComponentRelatedEventSubscription -> {
                    componentRelatedEventsSubscribers[eventSubscription.eventType.eventTypeId][eventSubscription.componentType.componentTypeId]
                        .removeContainingOrder(system as EventSystem<ComponentRelatedEvent<*>>)
                }
            }
        }
        configInternalEventsProducers(eventSubscriptions, added = false)
    }

    fun queueEvent(event: Event) = eventQueue.add(event)

    @Suppress("UNCHECKED_CAST")
    internal fun fireEvents() {
        var event = eventQueue.poll()
        while (event != null) {
            eventsToSystems[event.getEventTypeId()]
                .fireEvent(event)
            if (event is ComponentRelatedEvent<*>) {
                componentRelatedEventsSubscribers[event.getEventTypeId()][event.component.getComponentTypeId()]
                    .fireEvent(event)
            }
            if (event is Poolable) {
                event.returnToPool()
            }
            event = eventQueue.poll()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun IterableArray<out EventSystem<*>>.fireEvent(event: Event) {
        (this as IterableArray<EventSystem<in Event>>).forEach {
            world.updateSubscriptions()
            if (it.enabled) {
                it.beforeActing()
                it.fireEvent(event)
                it.afterActing()
            }
        }
    }

    /** Config internal event producers to fire events only if any of the registered systems is interested in those events.*/
    private fun configInternalEventsProducers(eventSubscriptions: List<EventSubscription>, added: Boolean) {
        var hasChanges = false
        for (eventSubscription in eventSubscriptions) {
            val eventType = eventSubscription.eventType
            if (!eventType.isSubclassOf(InternalEvent::class)) continue
            if (eventSubscription is SimpleEventSubscription) {
                when(eventType) {
                    ComponentAddedEvent::class -> {
                        internalEventsInterests.componentAddedEventsInterestsAmount.changeEach { it + if (added) 1 else -1 }
                        hasChanges = true
                    }
                    ComponentRemovedEvent::class -> {
                        internalEventsInterests.componentRemovedEventsInterestsAmount.changeEach { it + if (added) 1 else -1 }
                        hasChanges = true
                    }
                    EntityAddedEvent::class -> {
                        internalEventsInterests.entityAddedInterestsAmount += if (added) 1 else -1
                        hasChanges = true
                    }
                    EntityRemovedEvent::class -> {
                        internalEventsInterests.entityRemovedInterestsAmount += if (added) 1 else -1
                        hasChanges = true
                    }
                    ComponentChangedEvent::class -> {
                        internalEventsInterests.componentChangedEventsInterestsAmount.changeEach { it + if (added) 1 else -1 }
                        hasChanges = true
                    }
                }
            }
            else if (eventSubscription is ComponentRelatedEventSubscription) {
                val componentTypeId = eventSubscription.componentType.componentTypeId
                when(eventType) {
                    ComponentAddedEvent::class -> {
                        internalEventsInterests.componentAddedEventsInterestsAmount[componentTypeId] += if (added) 1 else -1
                        hasChanges = true
                    }
                    ComponentRemovedEvent::class -> {
                        internalEventsInterests.componentRemovedEventsInterestsAmount[componentTypeId] += if (added) 1 else -1
                        hasChanges = true
                    }
                    ComponentChangedEvent::class -> {
                        internalEventsInterests.componentChangedEventsInterestsAmount[componentTypeId] += if (added) 1 else -1
                        hasChanges = true
                    }
                }
            }
        }

        if (!hasChanges) return

        internalEventsInterests.componentAddedEventsInterestsAmount.forEachIndexed { id, amount ->
            world.entityMapper.componentMappers[id].sendComponentAddedEvents = amount > 0
        }
        internalEventsInterests.componentRemovedEventsInterestsAmount.forEachIndexed { id, amount ->
            world.entityMapper.componentMappers[id].sendComponentRemovedEvents = amount > 0
        }
        internalEventsInterests.componentChangedEventsInterestsAmount.forEachIndexed { id, amount ->
            world.observableComponentChangeManager.sendComponentChangedEvents[id] = amount > 0
        }
        world.entityMapper.sendEntityAddedEvents = internalEventsInterests.entityAddedInterestsAmount > 0
        world.entityMapper.sendEntityRemovedEvents = internalEventsInterests.entityRemovedInterestsAmount > 0
    }


    private inner class InternalEventsInterests {

        val componentAddedEventsInterestsAmount = IntArray(ExEcs.componentTypeIDsResolver.size)
        val componentRemovedEventsInterestsAmount = IntArray(ExEcs.componentTypeIDsResolver.size)
        val componentChangedEventsInterestsAmount = IntArray(ExEcs.componentTypeIDsResolver.size)
        var entityAddedInterestsAmount = 0
        var entityRemovedInterestsAmount = 0

    }

}