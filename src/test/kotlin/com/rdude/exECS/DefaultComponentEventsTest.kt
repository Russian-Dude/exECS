package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.system.SimpleEventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DefaultComponentEventsTest {

    private inner class TestComponent : Component

    private inner class ComponentAddedSystem : SimpleEventSystem<ComponentAddedEvent>() {
        var component: Component? = null
        override fun eventFired(event: ComponentAddedEvent) {
            component = event.component
        }
    }

    private inner class ComponentRemovedSystem : SimpleEventSystem<ComponentRemovedEvent>() {
        var component: Component? = null
        override fun eventFired(event: ComponentRemovedEvent) {
            component = event.component
        }
    }

    private val world = World()

    private val componentAddedSystem = ComponentAddedSystem()
    private val componentRemovedSystem = ComponentRemovedSystem()

    private val component = TestComponent()
    private val entity = world.createEntity()


    @BeforeAll
    fun registerSystems() {
        world.addSystem(componentAddedSystem)
        world.addSystem(componentRemovedSystem)
    }

    @Test
    @Order(1)
    fun componentAddedTest() {
        entity.addComponent(component)
        world.act(0.0)
        assert(componentAddedSystem.component == component)
    }

    @Test
    @Order(2)
    fun componentRemovedTest() {
        entity.removeComponent(TestComponent::class)
        world.act(0.0)
        assert(componentRemovedSystem.component == component)
    }


}