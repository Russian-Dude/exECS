package com.rdude.exECS

import com.rdude.exECS.system.SimpleActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SystemInjectionTest {

    private inner class System1 : SimpleActingSystem() {
        override fun act(delta: Double) { }
    }

    private inner class System2: SimpleActingSystem() {
        override fun act(delta: Double) { }
    }

    private inner class System3: SimpleActingSystem() {
        val system1 by inject<System1>()
        val system2 by inject<System2>()
        override fun act(delta: Double) { }
    }

    private val world = World()

    private val system1 = System1()
    private val system2 = System2()
    private val system3 = System3()

    @BeforeAll
    fun registerSystems() {
        world.addSystem(system1)
        world.addSystem(system3)
    }

    @Test
    fun mustBeInjected() {
        assert(system3.system1 == system1)
    }

    @Test
    fun mustNotBeInjected() {
        assertThrows<IllegalArgumentException> { system3.system2 == system2 }
    }

}