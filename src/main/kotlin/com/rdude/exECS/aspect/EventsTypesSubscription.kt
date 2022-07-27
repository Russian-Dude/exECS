package com.rdude.exECS.aspect

import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.event.Event
import com.rdude.exECS.event.ComponentRelatedEvent
import kotlin.reflect.KClass

/** Stores the event types to which EventSystem is subscribed.
 *
 * Events subscription is shared between different [EventSystems][EventSystem] with equal Events subscriptions.
 * @param declaredClass class declared in System's generic
 * @param eventIds type ids of [Events][Event]
 * @param componentIds type ids of components if [Event] is [ComponentRelatedEvent]*/
internal class EventsTypesSubscription(
    internal val declaredClass: KClass<out Event>,
    internal val eventIds: IntArray,
    internal val componentIds: IntArray
)