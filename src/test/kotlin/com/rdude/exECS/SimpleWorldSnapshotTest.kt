package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.Event
import com.rdude.exECS.serialization.SimpleWorldSnapshot
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleWorldSnapshotTest : WorldAccessor() {

    class TestComponent1 : Component

    class TestComponent2 : Component

    class TestEvent1 : Event

    override val world = World()
    lateinit var world2: World
    lateinit var snapshot1: SimpleWorldSnapshot
    lateinit var snapshot2: SimpleWorldSnapshot

    @BeforeAll
    fun init() {
        val e1 = createEntity(TestComponent1(), TestComponent2())
        val e2 = createEntity(TestComponent1(), TestComponent2())
        val e3 = createEntity(TestComponent2())
        val e4 = createEntity(TestComponent1())
        e1.addChild(e2)
        e2.addChild(e3)
        e2.addChild(e4)
        world.act()
        queueEvent(TestEvent1())
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

    @Test
    fun parentChildRelations() {
        assert(snapshot1.entitiesParentChildRelationsSnapshot.data.contentEquals(snapshot2.entitiesParentChildRelationsSnapshot.data))
    }

}