package com.rdude.exECS

import com.rdude.exECS.event.Event
import com.rdude.exECS.event.EventPriority
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EventsPriorityTest {

    private abstract class TestEvent(var value: Int = -1) : Event

    private class LowPriorityEvent(value: Int = 0) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.LOW
    }

    private class MediumPriorityEvent(value: Int = 0) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.MEDIUM
    }

    private class HighPriorityEvent(value: Int = 0) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.HIGH
    }

    private class CriticalPriorityEvent(value: Int = 0) : TestEvent(value) {
        override fun defaultPriority(): EventPriority = EventPriority.CRITICAL
    }

    private class LowPriorityPoolableEvent(value: Int = 0) : TestEvent(value), Poolable {
        override fun defaultPriority(): EventPriority = EventPriority.LOW
    }

    private class MediumPriorityPoolableEvent(value: Int = 0) : TestEvent(value), Poolable {
        override fun defaultPriority(): EventPriority = EventPriority.MEDIUM
    }

    private class HighPriorityPoolableEvent(value: Int = 0) : TestEvent(value), Poolable {
        override fun defaultPriority(): EventPriority = EventPriority.HIGH
    }

    private class CriticalPriorityPoolableEvent(value: Int = 0) : TestEvent(value), Poolable {
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
    @Order(0)
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
    @Order(1)
    fun defaultPriorityWithPoolalbe() {
        val e1 = fromPool<LowPriorityPoolableEvent> { value = 1 }
        val e2 = fromPool<LowPriorityPoolableEvent> { value = 2 }
        val e3 = fromPool<CriticalPriorityPoolableEvent> { value = 3 }
        val e4 = fromPool<MediumPriorityPoolableEvent> { value = 4 }
        val e5 = fromPool<HighPriorityPoolableEvent> { value = 5 }
        e5.returnToPool()
        e4.returnToPool()
        e3.returnToPool()
        e2.returnToPool()
        e1.returnToPool()
        world.queueEvent<LowPriorityPoolableEvent>()
        world.queueEvent<LowPriorityPoolableEvent>()
        world.queueEvent<CriticalPriorityPoolableEvent>()
        world.queueEvent<MediumPriorityPoolableEvent>()
        world.queueEvent<HighPriorityPoolableEvent>()
        world.act()
        println("events values: ${testSystem.eventsValues}")
        assert(testSystem.eventsValues == listOf(3, 5, 4, 1, 2))
    }

    @Test
    @Order(2)
    fun customPriority() {
        world.queueEvent(LowPriorityEvent(1), EventPriority.CRITICAL)
        world.queueEvent(LowPriorityEvent(2), EventPriority.HIGH)
        world.queueEvent(CriticalPriorityEvent(3), EventPriority.LOW)
        world.queueEvent(MediumPriorityEvent(4), EventPriority.CRITICAL)
        world.queueEvent(HighPriorityEvent(5), EventPriority.MEDIUM)
        world.act()
        assert(testSystem.eventsValues == listOf(1, 4, 2, 5, 3))
    }

    @Test
    @Order(3)
    fun customPriorityWithPoolable() {
        val e1 = fromPool<LowPriorityPoolableEvent> { value = 1 }
        val e2 = fromPool<LowPriorityPoolableEvent> { value = 2 }
        val e3 = fromPool<CriticalPriorityPoolableEvent> { value = 3 }
        val e4 = fromPool<MediumPriorityPoolableEvent> { value = 4 }
        val e5 = fromPool<HighPriorityPoolableEvent> { value = 5 }
        e5.returnToPool()
        e4.returnToPool()
        e3.returnToPool()
        e2.returnToPool()
        e1.returnToPool()
        world.queueEvent<LowPriorityPoolableEvent>(EventPriority.CRITICAL)
        world.queueEvent<LowPriorityPoolableEvent>(EventPriority.HIGH)
        world.queueEvent<CriticalPriorityPoolableEvent>(EventPriority.LOW)
        world.queueEvent<MediumPriorityPoolableEvent>(EventPriority.CRITICAL)
        world.queueEvent<HighPriorityPoolableEvent>(EventPriority.MEDIUM)
        world.act()
        assert(testSystem.eventsValues == listOf(1, 4, 2, 5, 3))
    }

    @Test
    @Order(4)
    fun customPriorityWithPoolableAndApply() {
        world.queueEvent<LowPriorityPoolableEvent>(EventPriority.CRITICAL) { value = 1 }
        world.queueEvent<LowPriorityPoolableEvent>(EventPriority.HIGH) { value = 2 }
        world.queueEvent<CriticalPriorityPoolableEvent>(EventPriority.LOW) { value = 3 }
        world.queueEvent<MediumPriorityPoolableEvent>(EventPriority.CRITICAL) { value = 4 }
        world.queueEvent<HighPriorityPoolableEvent>(EventPriority.MEDIUM) { value = 5 }
        world.act()
        assert(testSystem.eventsValues == listOf(1, 4, 2, 5, 3))
    }

}