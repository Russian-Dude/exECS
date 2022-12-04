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

    private val cachedSubscriptions = CachedSubscriptions()

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> getEventsTypesSubscriptionForSystem(system: EventSystem<T>): EventsTypesSubscription {

        val supertype = system::class.allSupertypes.single { it.classifier == EventSystem::class }
        val arguments = supertype.arguments
        val eventType = arguments[0].type!!
        val eventKClass = eventType.classifier!! as KClass<T>

        // if event is component related.
        return if (eventKClass.isSubclassOf(ComponentRelatedEvent::class)) {
            val componentKClass = eventType.arguments[0].type?.classifier as? KClass<out Component> ?: Component::class

            val cachedSubscription = cachedSubscriptions[eventKClass, componentKClass]
            if (cachedSubscription != null) return cachedSubscription

            val offset = when(eventKClass) {
                ComponentAddedEvent::class -> EventTypeIDsResolver.COMPONENT_ADDED_ID_SHIFT
                ComponentRemovedEvent::class -> EventTypeIDsResolver.COMPONENT_REMOVED_ID_SHIFT
                ComponentChangedEvent::class -> EventTypeIDsResolver.COMPONENT_CHANGED_ID_SHIFT
                else -> throw NotImplementedError("Can not get offset for $eventKClass. This type is not yet implemented.")
            }
            val componentIds =
                // subscribe to all components
                if (componentKClass == Component::class)
                    IntArray(ExEcs.componentTypeIDsResolver.size) { it }
                else mutableListOf(componentKClass)
                    .apply { addAll(ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(componentKClass)) }
                    .map { it.componentTypeId }
                    .toIntArray()
            val eventIds = componentIds
                .map { EventTypeIDsResolver.INTERNAL_NON_COMPONENT_RELATED_EVENTS_AMOUNT + (offset * ExEcs.componentTypeIDsResolver.size) + it }
                .toIntArray()

            val eventsTypesSubscription = EventsTypesSubscription(eventKClass, eventIds, componentIds)
            cachedSubscriptions[eventKClass, componentKClass] = eventsTypesSubscription
            eventsTypesSubscription
        }
        // if event is simple
        else {

            val cachedSubscription = cachedSubscriptions[eventKClass]
            if (cachedSubscription != null) return cachedSubscription

            if (eventKClass == Event::class) {
                val subscription = EventsTypesSubscription(Event::class,
                    IntArray(ExEcs.eventTypeIDsResolver.size) { it },
                    intArrayOf()
                )
                cachedSubscriptions[eventKClass] = subscription
                return subscription
            }

            val eventClasses: MutableList<KClass<out Event>> =
                if (!eventKClass.isAbstract && !eventKClass.java.isInterface) mutableListOf(eventKClass)
                else mutableListOf()
            eventClasses.addAll(ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(eventKClass))
            val eventIds = eventClasses
                .map { it.eventTypeId }
                .toIntArray()

            val eventsTypesSubscription = EventsTypesSubscription(eventKClass, eventIds, intArrayOf())
            cachedSubscriptions[eventKClass] = eventsTypesSubscription
            eventsTypesSubscription
        }
    }

    private inner class CachedSubscriptions {

        private val cache = mutableMapOf<KClass<out Event>, CachedSubscriptionsEntry?>()

        operator fun get(eventClass: KClass<out Event>, innerGeneric: KClass<*>? = null): EventsTypesSubscription? =
            cache[eventClass]?.get(innerGeneric)

        operator fun set(
            eventClass: KClass<out Event>,
            innerGeneric: KClass<*>?,
            subscription: EventsTypesSubscription
        ) {
            val entry = CachedComponentRelatedSubscriptionsEntry()
            entry[innerGeneric] = subscription
            cache[eventClass] = entry
        }

        operator fun set(
            eventClass: KClass<out Event>,
            subscription: EventsTypesSubscription
        ) {
            val entry = CachedSimpleSubscriptionsEntry()
            entry[null] = subscription
            cache[eventClass] = entry
        }
    }

    private abstract inner class CachedSubscriptionsEntry {
        abstract operator fun get(innerGeneric: KClass<*>? = null): EventsTypesSubscription?

        abstract operator fun set(innerGeneric: KClass<*>?, subscription: EventsTypesSubscription)
    }

    private inner class CachedComponentRelatedSubscriptionsEntry : CachedSubscriptionsEntry() {

        private val subscriptionsByComponentClass = mutableMapOf<KClass<*>, EventsTypesSubscription?>()

        override fun get(innerGeneric: KClass<*>?): EventsTypesSubscription? =
            subscriptionsByComponentClass[innerGeneric]

        override fun set(innerGeneric: KClass<*>?, subscription: EventsTypesSubscription) {
            if (innerGeneric == null) throw IllegalStateException("Component related subscription without generic parameter.")
            subscriptionsByComponentClass[innerGeneric] = subscription
        }
    }

    private inner class CachedSimpleSubscriptionsEntry : CachedSubscriptionsEntry() {

        private var subscription: EventsTypesSubscription? = null

        override fun get(innerGeneric: KClass<*>?): EventsTypesSubscription? = subscription

        override fun set(innerGeneric: KClass<*>?, subscription: EventsTypesSubscription) {
            this.subscription = subscription
        }
    }

}