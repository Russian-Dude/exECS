package com.rdude.exECS.utils.reflection

import com.rdude.exECS.aspect.EventsTypesSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.event.*
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.utils.eventTypeId
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf

internal class EventSystemGenericQualifier {

    private val subscriptionsBySystemType = Array<EventsTypesSubscription?>(ExEcs.systemTypeIDsResolver.size) { null }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> getEventsTypesSubscriptionForSystem(system: EventSystem<T>): EventsTypesSubscription {

        val cached = subscriptionsBySystemType[system.typeId]
        if (cached != null) return cached

        val supertype = system::class.allSupertypes.single { it.classifier == EventSystem::class }
        val arguments = supertype.arguments
        val eventType = arguments[0].type!!
        val eventKClass = eventType.classifier!! as KClass<T>

        val eventIds: IntArray
        val componentIds: IntArray

        // if event is component related.
        if (eventKClass.isSubclassOf(ComponentRelatedEvent::class)) {
            val componentKClass = eventType.arguments[0].type?.classifier as? KClass<out Component> ?: Component::class
            val offset = when(eventKClass) {
                ComponentAddedEvent::class -> 0
                ComponentRemovedEvent::class -> 1
                ComponentChangedEvent::class -> 2
                else -> throw NotImplementedError("Can not get offset for $eventKClass. This type is not yet implemented.")
            }
            componentIds =
                // subscribe to all components
                if (componentKClass == Component::class)
                    IntArray(ExEcs.componentTypeIDsResolver.size) { it }
                else mutableListOf(componentKClass)
                    .apply { addAll(ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(componentKClass)) }
                    .map { it.componentTypeId }
                    .toIntArray()
            eventIds = componentIds
                .map { 3 + (offset * ExEcs.componentTypeIDsResolver.size) + it } // ugly but performant
                .toIntArray()
        }
        // if event is simple
        else {
            componentIds = intArrayOf()
            val eventClasses: MutableList<KClass<out Event>> =
                if (!eventKClass.isAbstract && !eventKClass.java.isInterface) mutableListOf(eventKClass)
                else mutableListOf()
            eventClasses.addAll(ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(eventKClass))
            eventIds = eventClasses
                .map { it.eventTypeId }
                .toIntArray()
        }

        val eventsTypesSubscription = EventsTypesSubscription(eventKClass, eventIds, componentIds)
        subscriptionsBySystemType[system.typeId] = eventsTypesSubscription

        return eventsTypesSubscription
    }

}