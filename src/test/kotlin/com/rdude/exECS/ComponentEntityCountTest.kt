package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.PoolableComponent
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ComponentEntityCountTest {

    private class TestComponent1 : PoolableComponent

    private class TestComponent2 : Component, Poolable

    private inner class ComponentRemoverSystem(var removeCount: Int) :
        ActingSystem(anyOf = TestComponent1::class and TestComponent2::class) {

        override fun act(entity: EntityWrapper, delta: Double) {
            if (removeCount > 0) {
                entity.removeComponent<TestComponent1>()
                entity.removeComponent<TestComponent2>()
                removeCount--
            }
        }
    }

    @Test
    fun addToEntities() {
        val world = World()
        val component = TestComponent1()
        for (i in 0 until 3) {
            world.createEntity(component)
        }
        assert(component.insideEntities == 3)
    }

    @Test
    fun addToEntities2() {
        val world = World()
        val component = TestComponent2()
        for (i in 0 until 3) {
            world.createEntity(component)
        }
        assert(PoolableComponent.componentsToInsideEntitiesAmount[component] == 3)
    }

    @Test
    fun removeFromEntities() {
        val world = World().apply { addSystem(ComponentRemoverSystem(2)) }
        val component = TestComponent1()
        for (i in 0 until 10) {
            world.createEntity(component)
        }
        for (i in 0..10) {
            world.act(0.0)
        }
        assert(component.insideEntities == 8)
    }

    @Test
    fun removeFromEntities2() {
        val world = World().apply { addSystem(ComponentRemoverSystem(2)) }
        val component = TestComponent2()
        for (i in 0 until 10) {
            world.createEntity(component)
        }
        for (i in 0..10) {
            world.act(0.0)
        }
        assert(PoolableComponent.componentsToInsideEntitiesAmount[component] == 8)
    }

    @Test
    fun removeFromEntitiesStopOnZero() {
        val world = World().apply { addSystem(ComponentRemoverSystem(20)) }
        val component = TestComponent1()
        for (i in 0..10) {
            world.createEntity(component)
        }
        for (i in 0..20) {
            world.act(0.0)
        }
        assert(component.insideEntities == 0)
    }

    @Test
    fun removeFromEntitiesStopOnZero2() {
        val world = World().apply { addSystem(ComponentRemoverSystem(20)) }
        val component = TestComponent2()
        for (i in 0..10) {
            world.createEntity(component)
        }
        for (i in 0..20) {
            world.act(0.0)
        }
        assert(PoolableComponent.componentsToInsideEntitiesAmount[component] == 0)
    }

}