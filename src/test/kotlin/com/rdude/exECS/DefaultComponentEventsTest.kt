package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.system.SimpleEventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DefaultComponentEventsTest {

    private inner class TestComponent : Component
    private inner class StartComponent : Component

    private inner class NeedToRemoveComponentEvent : Event
    private inner class NeedToAddComponent : Event

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

    private inner class RemoveComponentSystem : EventSystem<NeedToRemoveComponentEvent>(only = TestComponent::class) {
        override fun eventFired(entity: EntityWrapper, event: NeedToRemoveComponentEvent) {
            entity.removeComponent<TestComponent>()
        }
    }

    private inner class AddComponentSystem : EventSystem<NeedToAddComponent>(only = StartComponent::class) {
        override fun eventFired(entity: EntityWrapper, event: NeedToAddComponent) {
            entity += component
        }
    }

    private val world = World()

    private val componentAddedSystem = ComponentAddedSystem()
    private val componentRemovedSystem = ComponentRemovedSystem()
    private val removeComponentSystem = RemoveComponentSystem()
    private val addComponentSystem = AddComponentSystem()

    private val component = TestComponent()


    @BeforeAll
    fun registerSystems() {
        world.addSystem(componentAddedSystem)
        world.addSystem(componentRemovedSystem)
        world.addSystem(removeComponentSystem)
        world.addSystem(addComponentSystem)
    }

    @Test
    @Order(1)
    fun componentAddedTest() {
        world.createEntity(StartComponent())
        world.queueEvent(NeedToAddComponent())
        world.act(0.0)
        assert(componentAddedSystem.component == component)
    }

    @Test
    @Order(2)
    fun componentRemovedTest() {
        world.queueEvent(NeedToRemoveComponentEvent())
        world.act(0.0)
        assert(componentRemovedSystem.component == component)
    }


}