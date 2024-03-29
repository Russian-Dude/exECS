package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SingletonsTest {

    class TestSingleton : SingletonEntity() {
        var testValue = 0
    }

    class TestComponent : Component

    class TestSystem : IterableActingSystem(only = TestComponent::class) {
        var foundComponent: TestComponent? = null
        override fun act(entity: Entity) {
            foundComponent = entity.getComponent()
        }
    }

    @Test
    fun test() {
        val world = World()
        val singleton = TestSingleton()
        val system = TestSystem()
        world.registerSystem(system)
        world.addSingletonEntity(singleton)
        singleton.addComponent(TestComponent())
        world.act()
        assert(system.foundComponent == singleton.getComponent<TestComponent>())
    }

}