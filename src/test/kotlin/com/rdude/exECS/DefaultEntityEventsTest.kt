package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.system.IterableEventSystem
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DefaultEntityEventsTest {

    private inner class Component1 : Component
    private inner class Component2 : Component

    private inner class CheckComponent : Component

    private inner class NeedToRemoveEntityEvent : Event

    private inner class EntityAddedSystem : EventSystem<EntityAddedEvent>() {
        var component: CheckComponent? = null
        override fun eventFired(event: EntityAddedEvent) {
            component = event.entity.getComponent()
        }
    }

    private inner class RemoveEntitySystem : IterableEventSystem<NeedToRemoveEntityEvent>(only = CheckComponent::class) {
        override fun eventFired(entity: Entity, event: NeedToRemoveEntityEvent) {
            entity.remove()
        }
    }

    private inner class EntityRemovedSystem : EventSystem<EntityRemovedEvent>() {
        var component: CheckComponent? = null
        override fun eventFired(event: EntityRemovedEvent) {
            component = event.entity.getComponent()
        }
    }

    private inner class EntityAddedWithConcreteComponentSystem : IterableEventSystem<EntityAddedEvent>(only = Component1::class) {
        var component: CheckComponent? = null
        override fun eventFired(entity: Entity, event: EntityAddedEvent) {
            component = event.entity.getComponent()
        }
    }

    private val world = World()
    private var checkComponent = CheckComponent()

    private val entityAddedSystem = EntityAddedSystem()
    private val entityRemovedSystem = EntityRemovedSystem()
    private val entityAddedWithConcreteComponentSystem = EntityAddedWithConcreteComponentSystem()
    private val removeEntitySystem = RemoveEntitySystem()

    @BeforeAll
    fun registerSystems() {
        world.registerSystem(entityAddedSystem)
        world.registerSystem(entityRemovedSystem)
        world.registerSystem(entityAddedWithConcreteComponentSystem)
        world.registerSystem(removeEntitySystem)
    }

    @Test
    @Order(1)
    fun entityAdded() {
        world.createEntity(checkComponent)
        world.act()
        assertAll(
            { assert(entityAddedSystem.component == checkComponent) },
            { assert(entityRemovedSystem.component == null) })
    }

    @Test
    @Order(2)
    fun entityRemoved() {
        world.queueEvent(NeedToRemoveEntityEvent())
        world.act()
        assert(entityRemovedSystem.component == checkComponent)
    }

    @Test
    @Order(3)
    fun entityWithComponentsWasNotAddedYet() {
        world.act()
        assert(entityAddedWithConcreteComponentSystem.component == null)
    }

    @Test
    @Order(4)
    fun entityWithWrongComponentAdded() {
        world.createEntity(Component2())
        world.act()
        assert(entityAddedWithConcreteComponentSystem.component == null)
    }

    @Test
    @Order(5)
    fun entityWithCorrectComponentAdded() {
        checkComponent = CheckComponent()
        world.createEntity(checkComponent, Component1())
        world.act()
        assert(entityAddedWithConcreteComponentSystem.component == checkComponent)
    }

}