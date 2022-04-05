package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ComponentEntityCountTest {

    private inner class TestComponent : Component

    private inner class ComponentRemoverSystem(var removeCount: Int) : ActingSystem(only = TestComponent::class) {

        override fun act(entity: EntityWrapper, delta: Double) {
            if (removeCount > 0) {
                entity.removeComponent<TestComponent>()
                removeCount--
            }
        }
    }

    @Test
    fun addToEntities() {
        val world = World()
        val component = TestComponent()
        for (i in 0 until 3) {
            world.createEntity(component)
        }
        assert(component.insideEntities == 3)
    }

    @Test
    fun removeFromEntities() {
        val world = World().apply { addSystem(ComponentRemoverSystem(2)) }
        val component = TestComponent()
        for (i in 0 until 10) {
            world.createEntity(component)
        }
        for (i in 0..10) {
            world.act(0.0)
        }
        assert(component.insideEntities == 8)
    }

    @Test
    fun removeFromEntitiesStopOnZero() {
        val world = World().apply { addSystem(ComponentRemoverSystem(20)) }
        val component = TestComponent()
        for (i in 0..10) {
            world.createEntity(component)
        }
        for (i in 0..20) {
            world.act(0.0)
        }
        assert(component.insideEntities == 0)
    }

}