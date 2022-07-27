package com.rdude.exECS

import com.rdude.exECS.event.Event
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PolymorphicEventsTest {

    open inner class ParentEvent : Event
    inner class ChildEvent1 : ParentEvent()
    inner class ChildEvent2 : ParentEvent()

    private inner class ChildEventSystem1 : EventSystem<ChildEvent1>() {
        var fired = false
        override fun eventFired(event: ChildEvent1) {
            fired = true
        }
    }

    private inner class ChildEventSystem2 : EventSystem<ChildEvent2>() {
        var fired = false
        override fun eventFired(event: ChildEvent2) {
            fired = true
        }
    }

    private inner class ParentEventSystem : EventSystem<ParentEvent>() {
        var fired = false
        override fun eventFired(event: ParentEvent) {
            fired = true
        }
    }

    private val world = World()
    private val childEventSystem1 = ChildEventSystem1()
    private val childEventSystem2 = ChildEventSystem2()
    private val parentEventSystem = ParentEventSystem()

    @BeforeAll
    fun registerSystems() {
        world.registerSystem(childEventSystem1)
        world.registerSystem(childEventSystem2)
        world.registerSystem(parentEventSystem)
    }

    @BeforeEach
    fun resetFiredFlagOnAllSystems() {
        childEventSystem1.fired = false
        childEventSystem2.fired = false
        parentEventSystem.fired = false
    }

    @Test
    fun parentEventSystemTriggeredByChildEvents() {
        world.queueEvent(ChildEvent1())
        world.act(0.0)
        assert(parentEventSystem.fired)
    }

    @Test
    fun childEventSystemNotTriggeredByParentEvents() {
        world.queueEvent(ParentEvent())
        world.act(0.0)
        assertAll(
            { assertFalse(childEventSystem1.fired) },
            { assertFalse(childEventSystem2.fired) }
        )
    }

}