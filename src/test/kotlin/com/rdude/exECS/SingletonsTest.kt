package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingletonsTest {

    class TestSingleton : SingletonEntity() {
        var testValue = 0
    }

    class TestComponent : Component

    class TestSystem : ActingSystem(only = TestComponent::class) {
        var foundComponent: TestComponent? = null
        override fun act(entity: EntityWrapper, delta: Double) {
            foundComponent = entity.getComponent()
        }
    }

    @Test
    fun test() {
        val world = World()
        val singleton = TestSingleton()
        val system = TestSystem()
        world.addSystem(system)
        world.addSingletonEntity(singleton)
        singleton.addComponent(TestComponent())
        world.act(0.0)
        assert(system.foundComponent == singleton.getComponent<TestComponent>())
    }

}