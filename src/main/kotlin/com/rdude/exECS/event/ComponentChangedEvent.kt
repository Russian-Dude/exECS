package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.ExEcs


/** Every time an [ObservableComponent] of type [T] is changed, if at least one [EventSystem] is subscribed to this Event
 * with type [T], this event is queued.*/
class ComponentChangedEvent<T : ObservableComponent<*>> : InternalPoolableEvent(), ComponentRelatedEvent<T> {

    /** The [Component] that has been changed.*/
    override lateinit var component: T
        internal set

    /** The change that happened to the [component].*/
    @JvmField
    internal var _change: ComponentChange = NO_CHANGE

    override fun getEventTypeId(): Int =
        EventTypeIDsResolver.INTERNAL_NON_COMPONENT_RELATED_EVENTS_AMOUNT +
                EventTypeIDsResolver.COMPONENT_CHANGED_ID_SHIFT * ExEcs.componentTypeIDsResolver.size +
                component.getComponentTypeId()


    internal companion object {

        val pool = Pool { ComponentChangedEvent<ObservableComponent<*>>() }

        private val NO_CHANGE = object : ComponentChange {}

    }

}

// This way of declaring property helps to avoid second generic in the ComponentChangedEvent.
// class MySystem : SimpleEventSystem<ComponentChangedEvent<ScoreComponent>>()
// looks nicer than
// class MySystem : SimpleEventSystem<ComponentChangedEvent<ScoreComponent>, ScoreChangedEvent>>
/** The change that happened to the component.*/
@Suppress("UNCHECKED_CAST")
val <CH : ComponentChange, CO : ObservableComponent<CH>, E : ComponentChangedEvent<CO>> E.change: CH get() = _change as CH

