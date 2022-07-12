package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.UniqueComponent
import com.rdude.exECS.world.World
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UniqueComponentTest {

    private class TestUniqueComponent : UniqueComponent

    private class SimpleTestComponent : Component

    @Test
    fun getAnotherComponent() {
        val world = World()
        val uniqueComponent = TestUniqueComponent()
        val simpleComponent = SimpleTestComponent()
        world.createEntity(uniqueComponent, simpleComponent)
        assert(uniqueComponent.getEntity().getComponent<SimpleTestComponent>(world) == simpleComponent)
    }

    @Test
    fun throwIfAddToAnotherEntity() {
        val world = World()
        val uniqueComponent = TestUniqueComponent()
        world.createEntity(uniqueComponent)
        assertThrows<IllegalStateException> { world.createEntity(uniqueComponent) }
    }

    @Test
    fun removeAndAdd() {
        val world = World()
        val uniqueComponent = TestUniqueComponent()
        world.createEntity(uniqueComponent)
        uniqueComponent.getEntity().remove(world)
        world.act(0.0)
        world.createEntity(uniqueComponent)
    }

}