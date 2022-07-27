package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.Event
import com.rdude.exECS.serialization.SimpleWorldSnapshot
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleWorldSnapshotTest {

    class TestComponent1 : Component

    class TestComponent2 : Component

    class TestEvent1 : Event

    class TestSystem1 : IterableActingSystem(anyOf = TestComponent1::class and TestComponent2::class) {
        override fun act(entity: Entity, delta: Double) {}
    }

    class TestSystem2 : EventSystem<TestEvent1>() {
        override fun eventFired(event: TestEvent1) {}
    }

    val world = World()
    lateinit var world2: World
    lateinit var snapshot1: SimpleWorldSnapshot
    lateinit var snapshot2: SimpleWorldSnapshot

    @BeforeAll
    fun init() {
        world.registerSystem(TestSystem1())
        world.registerSystem(TestSystem2())
        world.createEntity(TestComponent1(), TestComponent2())
        world.createEntity(TestComponent1(), TestComponent2())
        world.createEntity(TestComponent2())
        world.createEntity(TestComponent1())
        world.act(0.0)
        world.queueEvent(TestEvent1())
        snapshot1 = world.snapshot()
        world2 = World(snapshot1)
        snapshot2 = world2.snapshot()
    }

    @Test
    fun componentsEquals() {
        assert(
            world.entityMapper.componentMappers.withIndex()
                .all { (index, mapper) -> world2.entityMapper.componentMappers[index].backingArray.contentEquals(mapper.backingArray) }
        )
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
    fun amountEquals() {
        assert(snapshot1.simpleEntitiesAmount == snapshot2.simpleEntitiesAmount)
    }

}