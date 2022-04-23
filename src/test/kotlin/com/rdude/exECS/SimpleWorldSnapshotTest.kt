package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.Event
import com.rdude.exECS.serialization.SimpleWorldSnapshot
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.system.SimpleEventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleWorldSnapshotTest {

    class TestComponent1 : Component

    class TestComponent2 : Component

    class TestEvent1 : Event

    class TestSystem1 : ActingSystem(anyOf = TestComponent1::class and TestComponent2::class) {
        override fun act(entity: EntityWrapper, delta: Double) {}
    }

    class TestSystem2 : SimpleEventSystem<TestEvent1>() {
        override fun eventFired(event: TestEvent1) {}
    }

    val world = World()
    lateinit var snapshot1: SimpleWorldSnapshot
    lateinit var snapshot2: SimpleWorldSnapshot

    @BeforeAll
    fun init() {
        world.addSystem(TestSystem1())
        world.addSystem(TestSystem2())
        world.createEntity(TestComponent1(), TestComponent2())
        world.createEntity(TestComponent1(), TestComponent2())
        world.createEntity(TestComponent2())
        world.createEntity(TestComponent1())
        world.act(0.0)
        world.queueEvent(TestEvent1())
        snapshot1 = world.snapshot()
        world.systems.toList().forEach { world.removeSystem(it) }
        snapshot2 = World(snapshot1).snapshot()
    }

    @Test
    fun presenceEquals() {
        assert(
            snapshot1.componentMappers.withIndex().all { (index, mapperSnapshot) ->
                mapperSnapshot.presence.contentEquals(snapshot2.componentMappers[index].presence)
            }
        )
    }

    @Test
    fun dataEquals() {
        assert(
            snapshot1.componentMappers.withIndex().all { (index, mapperSnapshot) ->
                mapperSnapshot.data.contentEquals(snapshot2.componentMappers[index].data)
            }
        )
    }

    @Test
    fun eventsEquals() {
        assert(snapshot1.events.toTypedArray().contentEquals(snapshot2.events.toTypedArray()))
    }

    @Test
    fun amountEquals() {
        assert(snapshot1.simpleEntitiesAmount == snapshot2.simpleEntitiesAmount)
    }

    @Test
    fun systemsEquals() {
        var systemsEquals = true
        snapshot1.systems.forEachIndexed { index, systemSnapshot ->
            if (!systemSnapshot.subscriptions.contentEquals(snapshot2.systems[index].subscriptions)) {
                systemsEquals = false
                return@forEachIndexed
            }
            if (!systemSnapshot.systems.toTypedArray().contentEquals(snapshot2.systems[index].systems.toTypedArray())) {
                systemsEquals = false
                return@forEachIndexed
            }
        }
        assert(systemsEquals)
    }

}