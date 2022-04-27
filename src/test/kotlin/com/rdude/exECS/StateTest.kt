package com.rdude.exECS

import com.rdude.exECS.component.State
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StateTest {

    private inner class TestState : State

    private val state = TestState()

    private inner class System1 : ActingSystem(only = state) {
        var state: State? = null
        override fun act(entity: EntityWrapper, delta: Double) {
            state = entity.getComponent<TestState>()
        }
    }

    private inner class System2 : ActingSystem(only = TestState::class) {
        var state: State? = null
        override fun act(entity: EntityWrapper, delta: Double) {
            state = entity.getComponent<TestState>()
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
        world.addSystem(system1)
        world.addSystem(system2)
        world.createEntity(state)
        world.act(0.0)
    }

    @Test
    fun subscribedAsState() {
        assert(state == system1.state)
    }

    @Test
    fun subscribedAsComponent() {
        assert(state == system2.state)
    }



}