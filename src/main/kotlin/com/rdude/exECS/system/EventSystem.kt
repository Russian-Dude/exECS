package com.rdude.exECS.system

import com.rdude.exECS.event.Event
import com.rdude.exECS.world.World

/** System that Subscribes to [Events][Event]. Every time an Event of type [T] (or its subtype) is fired, [eventFired]
 * method is called.
 *
 * To subscribe to an Event, extend this class and pass Event type as a generic parameter.
 * Event type must be specified explicitly:
 * ```
 * // Correct
 * class MySystem : EventSystem<MyEvent>()
 * // Incorrect
 * class MySystem<T> : EventSystem<T>()
 * ```
 * Subscription to Events is polymorphic. System subscribed to an Event type of [T],
 * will also be subscribed to Event types that are subtypes of [T]:
 * ```
 * open class ColorChangedEvent : Event
 *
 * class BorderColorChangedEvent : ColorChangedEvent()
 *
 * // triggered only when BorderColorChangedEvent is fired
 * class MySystem1 : EventSystem<BorderColorChangedEvent>()
 *
 * // triggered when any of ColorChangedEvent or BorderColorChangedEvent is fired
 * class MySystem2 : EventSystem<ColorChangedEvent>()
 *
 * // triggered when any event is fired
 * class MySystem3 : EventSystem<Event>()
 * ```
 * @see System
 * @see IterableEventSystem
 * @see ActingSystem
 * @see IterableActingSystem*/
abstract class EventSystem<T : Event> : System() {

    /** If false this System will remain registered in the [World] but will not be triggered by Events.*/
    @JvmField var enabled = true

    /** Implement this method to specify a behaviour when an Event of type [T] (or its subtype) is fired.*/
    protected abstract fun eventFired(event: T)

    internal fun fireEvent(event: T) = eventFired(event)

}