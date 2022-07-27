package com.rdude.exECS

import com.rdude.exECS.component.ImmutableComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImmutableComponentTest {

    private inner class TestImmutableComponent : ImmutableComponent

    private val immutableComponent = TestImmutableComponent()

    private inner class System1 : IterableActingSystem(only = immutableComponent) {
        var immutableComponent: ImmutableComponent? = null
        override fun act(entity: Entity, delta: Double) {
            immutableComponent = entity.getComponent<TestImmutableComponent>()
        }
    }

    private inner class System2 : IterableActingSystem(only = TestImmutableComponent::class) {
        var immutableComponent: ImmutableComponent? = null
        override fun act(entity: Entity, delta: Double) {
            immutableComponent = entity.getComponent<TestImmutableComponent>()
        }
    }

    private lateinit var world: World
    private lateinit var system1: System1
    private lateinit var system2: System2

    @BeforeAll
    fun init() {
        world = World()
        system1 = System1()
        system2 = System2()
        world.registerSystem(system1)
        world.registerSystem(system2)
        world.createEntity(immutableComponent)
        world.act(0.0)
    }

    @Test
    fun subscribedAsState() {
        assert(immutableComponent == system1.immutableComponent)
    }

    @Test
    fun subscribedAsComponent() {
        assert(immutableComponent == system2.immutableComponent)
    }



}