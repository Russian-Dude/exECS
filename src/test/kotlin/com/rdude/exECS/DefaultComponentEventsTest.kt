package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
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

    private inner class AnyComponentAddedSystem1 : SimpleEventSystem<ComponentAddedEvent<*>>() {
        var component: Component? = null
        override fun eventFired(event: ComponentAddedEvent<*>) {
            component = event.component
        }
    }

    private inner class AnyComponentRemovedSystem1 : SimpleEventSystem<ComponentRemovedEvent<*>>() {
        var component: Component? = null
        override fun eventFired(event: ComponentRemovedEvent<*>) {
            component = event.component
        }
    }

    private inner class AnyComponentAddedSystem2 : SimpleEventSystem<ComponentAddedEvent<out Component>>() {
        var component: Component? = null
        override fun eventFired(event: ComponentAddedEvent<*>) {
            component = event.component
        }
    }

    private inner class AnyComponentRemovedSystem2 : SimpleEventSystem<ComponentRemovedEvent<out Component>>() {
        var component: Component? = null
        override fun eventFired(event: ComponentRemovedEvent<*>) {
            component = event.component
        }
    }

    private inner class TestComponentAddedSystem : SimpleEventSystem<ComponentAddedEvent<TestComponent>>() {
        var component: Component? = null
        override fun eventFired(event: ComponentAddedEvent<TestComponent>) {
            component = event.component
        }
    }

    private inner class TestComponentRemovedSystem : SimpleEventSystem<ComponentRemovedEvent<TestComponent>>() {
        var component: Component? = null
        override fun eventFired(event: ComponentRemovedEvent<TestComponent>) {
            component = event.component
        }
    }

    private inner class RemoveComponentSystem : EventSystem<NeedToRemoveComponentEvent>(only = TestComponent::class) {
        override fun eventFired(entity: Entity, event: NeedToRemoveComponentEvent) {
            entity.removeComponent<TestComponent>()
        }
    }

    private inner class AddComponentSystem : EventSystem<NeedToAddComponent>(only = StartComponent::class) {
        override fun eventFired(entity: Entity, event: NeedToAddComponent) {
            entity += component
        }
    }

    private val world = World()

    private val anyComponentAddedSystem1 = AnyComponentAddedSystem1()
    private val anyComponentRemovedSystem1 = AnyComponentRemovedSystem1()
    private val anyComponentAddedSystem2 = AnyComponentAddedSystem2()
    private val anyComponentRemovedSystem2 = AnyComponentRemovedSystem2()
    private val testComponentAddedSystem = TestComponentAddedSystem()
    private val testComponentRemovedSystem = TestComponentRemovedSystem()
    private val removeComponentSystem = RemoveComponentSystem()
    private val addComponentSystem = AddComponentSystem()

    private val component = TestComponent()


    @BeforeAll
    fun registerSystems() {
        world.addSystem(anyComponentAddedSystem1)
        world.addSystem(anyComponentRemovedSystem1)
        world.addSystem(anyComponentAddedSystem2)
        world.addSystem(anyComponentRemovedSystem2)
        world.addSystem(testComponentAddedSystem)
        world.addSystem(testComponentRemovedSystem)
        world.addSystem(removeComponentSystem)
        world.addSystem(addComponentSystem)
    }

    @Test
    @Order(1)
    fun componentAddedTest() {
        world.createEntity(StartComponent())
        world.queueEvent(NeedToAddComponent())
        world.act(0.0)
        assert(anyComponentAddedSystem1.component == component)
    }

    @Test
    @Order(2)
    fun componentAddedTest2() {
        assert(anyComponentAddedSystem2.component == component)
    }

    @Test
    @Order(3)
    fun componentAddedTest3() {
        assert(testComponentAddedSystem.component == component)
    }

    @Test
    @Order(4)
    fun componentRemovedTest() {
        world.queueEvent(NeedToRemoveComponentEvent())
        world.act(0.0)
        assert(anyComponentRemovedSystem1.component == component)
    }

    @Test
    @Order(5)
    fun componentRemovedTest2() {
        assert(anyComponentRemovedSystem2.component == component)
    }

    @Test
    @Order(6)
    fun componentRemovedTest3() {
        assert(testComponentRemovedSystem.component == component)
    }


}