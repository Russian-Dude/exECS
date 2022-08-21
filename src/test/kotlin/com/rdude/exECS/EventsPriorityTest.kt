package com.rdude.exECS

import com.rdude.exECS.event.Event
import com.rdude.exECS.event.EventPriority
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EventsPriorityTest {

    private abstract class TestEvent(val value: Int) : Event

    private class LowPriorityEvent(value: Int) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.LOW
    }

    private class MediumPriorityEvent(value: Int) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.MEDIUM
    }

    private class HighPriorityEvent(value: Int) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.HIGH
    }

    private class CriticalPriorityEvent(value: Int) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.CRITICAL
    }

    private class TestSystem : EventSystem<TestEvent>() {

        val eventsValues: MutableList<Int> = ArrayList()

        override fun eventFired(event: TestEvent) {
            eventsValues.add(event.value)
        }
    }


    private val testSystem = TestSystem()

    private val world = World()
        .apply { registerSystem(testSystem) }


    @BeforeEach
    fun clearList() {
        testSystem.eventsValues.clear()
    }

    @Test
    fun defaultPriority() {
        world.queueEvent(LowPriorityEvent(1))
        world.queueEvent(LowPriorityEvent(2))
        world.queueEvent(CriticalPriorityEvent(3))
        world.queueEvent(MediumPriorityEvent(4))
        world.queueEvent(HighPriorityEvent(5))
        world.act()
        assert(testSystem.eventsValues == listOf(3, 5, 4, 1, 2))
    }

    @Test
    fun customPriority() {
        world.queueEvent(LowPriorityEvent(1), EventPriority.CRITICAL)
        world.queueEvent(LowPriorityEvent(2), EventPriority.HIGH)
        world.queueEvent(CriticalPriorityEvent(3), EventPriority.LOW)
        world.queueEvent(MediumPriorityEvent(4), EventPriority.CRITICAL)
        world.queueEvent(HighPriorityEvent(5), EventPriority.MEDIUM)
        world.act()
        assert(testSystem.eventsValues == listOf(1, 4, 2, 5, 3))
    }

}