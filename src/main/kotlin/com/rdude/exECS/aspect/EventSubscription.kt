package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.Event
import kotlin.reflect.KClass

internal abstract class EventSubscription(@JvmField val eventType: KClass<out Event>)

/** Subscription to an [Event] type without type parameters.*/
internal class SimpleEventSubscription(eventType: KClass<out Event>) : EventSubscription(eventType) {

    override fun toString(): String {
        return "SimpleEventSubscription(eventType=$eventType)"
    }
}

/** Subscription to an [Event] type with [Component] type parameter.*/
internal class ComponentRelatedEventSubscription(eventType: KClass<out Event>, @JvmField val componentType: KClass<out Component>) :
    EventSubscription(eventType) {

    override fun toString(): String {
        return "ComponentRelatedEventSubscription(eventType=$eventType, componentType=$componentType)"
    }
}