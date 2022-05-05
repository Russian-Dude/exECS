package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.RichComponent
import com.rdude.exECS.world.World
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RichComponentTest {

    private class TestRichComponent : RichComponent

    private class SimpleTestComponent : Component

    @Test
    fun getAnotherComponent() {
        val world = World()
        val richComponent = TestRichComponent()
        val simpleComponent = SimpleTestComponent()
        world.createEntity(richComponent, simpleComponent)
        assert(richComponent.getEntity().getComponent<SimpleTestComponent>(world) == simpleComponent)
    }

    @Test
    fun throwIfAddToAnotherEntity() {
        val world = World()
        val richComponent = TestRichComponent()
        world.createEntity(richComponent)
        assertThrows<IllegalStateException> { world.createEntity(richComponent) }
    }

    @Test
    fun removeAndAdd() {
        val world = World()
        val richComponent = TestRichComponent()
        world.createEntity(richComponent)
        richComponent.getEntity().remove(world)
        world.act(0.0)
        world.createEntity(richComponent)
    }

}