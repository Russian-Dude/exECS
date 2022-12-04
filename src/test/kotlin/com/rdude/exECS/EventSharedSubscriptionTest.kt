package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.eventsTypesSubscription
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventSharedSubscriptionTest {

    private inner class TestEvent : Event

    private inner class TestEventAnother : Event

    private inner class TestComponent : Component

    private inner class AnotherComponent : Component

    private inner class SimpleSubscriptionSystem1 : EventSystem<TestEvent>() {
        override fun eventFired(event: TestEvent) {}
    }

    private inner class SimpleSubscriptionSystem2 : EventSystem<TestEvent>() {
        override fun eventFired(event: TestEvent) {}
    }

    private inner class SimpleSubscriptionSystemAnother : EventSystem<TestEventAnother>() {
        override fun eventFired(event: TestEventAnother) {}
    }

    private inner class SimpleSubscriptionSystemSuper : EventSystem<Event>() {
        override fun eventFired(event: Event) {}
    }

    private inner class ComponentAddedSystem1 : EventSystem<ComponentAddedEvent<TestComponent>>() {
        override fun eventFired(event: ComponentAddedEvent<TestComponent>) {}
    }

    private inner class ComponentAddedSystem2 : EventSystem<ComponentAddedEvent<TestComponent>>() {
        override fun eventFired(event: ComponentAddedEvent<TestComponent>) {}
    }

    private inner class ComponentAddedSystemAnother : EventSystem<ComponentAddedEvent<AnotherComponent>>() {
        override fun eventFired(event: ComponentAddedEvent<AnotherComponent>) {}
    }

    @Test
    fun simpleShared() {
        assert(SimpleSubscriptionSystem1().eventsTypesSubscription === SimpleSubscriptionSystem2().eventsTypesSubscription)
    }

    @Test
    fun differentSimpleNotShared() {
        assert(SimpleSubscriptionSystem1().eventsTypesSubscription !== SimpleSubscriptionSystemAnother().eventsTypesSubscription)
    }

    @Test
    fun simpleAndSuperEventNotShared() {
        assert(SimpleSubscriptionSystem1().eventsTypesSubscription !== SimpleSubscriptionSystemSuper().eventsTypesSubscription)
    }

    @Test
    fun simpleAndNotSimpleNotShared() {
        assert(SimpleSubscriptionSystem1().eventsTypesSubscription !== ComponentAddedSystem1().eventsTypesSubscription)
    }

    @Test
    fun componentRelatedShared() {
        assert(ComponentAddedSystem1().eventsTypesSubscription === ComponentAddedSystem2().eventsTypesSubscription)
    }

    @Test
    fun differentComponentRelatedNotShared() {
        assert(ComponentAddedSystem1().eventsTypesSubscription !== ComponentAddedSystemAnother().eventsTypesSubscription)
    }

}