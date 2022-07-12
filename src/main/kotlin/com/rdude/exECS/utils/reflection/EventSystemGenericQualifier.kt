package com.rdude.exECS.utils.reflection

import com.rdude.exECS.aspect.ComponentRelatedEventSubscription
import com.rdude.exECS.aspect.EventSubscription
import com.rdude.exECS.aspect.SimpleEventSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentRelatedEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.system.EventSystem
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf

internal class EventSystemGenericQualifier {

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> generateEventSubscription(system: EventSystem<T>) : EventSubscription {

        val supertype = system::class.allSupertypes.single { it.classifier == EventSystem::class }
        val arguments = supertype.arguments
        val eventType = arguments[0].type!!
        val eventKClass = eventType.classifier!! as KClass<T>

        val eventSubscription =
            // component related subscription
            if (eventKClass.isSubclassOf(ComponentRelatedEvent::class)) {
                val componentKClass = eventType.arguments[0].type?.classifier as? KClass<out Component> ?: Component::class
                if (componentKClass == Component::class) SimpleEventSubscription(eventKClass)
                else ComponentRelatedEventSubscription(eventKClass, componentKClass)
            }
            // simple subscription
            else {
                SimpleEventSubscription(eventKClass)
            }

        return eventSubscription
    }

    fun getEventSubscriptionsForSystem(system: EventSystem<*>) : List<EventSubscription> {
        val mainClass = generateEventSubscription(system)
        return Package.getPackages()
            .asSequence()
            .map { it.name.substringBefore('.') }
            .distinct()
            .map { Reflections(it) }
            .flatMap { it.getSubTypesOf(mainClass.eventType.java) }
            .map { it.kotlin }
            .filterNot { it.isAbstract }
            .map { SimpleEventSubscription(it) as EventSubscription }
            .toMutableList()
            .apply { add(mainClass) }
    }

}