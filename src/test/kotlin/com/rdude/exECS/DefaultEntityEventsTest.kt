package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.system.SimpleEventSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*
import kotlin.test.assertContains

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class DefaultEntityEventsTest {

    private inner class Component1 : Component
    private inner class Component2 : Component

    private inner class EntityAddedSystem : SimpleEventSystem<EntityAddedEvent>() {
        var entity: Entity? = null
        override fun eventFired(event: EntityAddedEvent) {
            entity = event.entity
        }
    }

    private inner class EntityRemovedSystem : SimpleEventSystem<EntityRemovedEvent>() {
        var entity: Entity? = null
        override fun eventFired(event: EntityRemovedEvent) {
            entity = event.entity
        }
    }

    private inner class EntityAddedWithConcreteComponentSystem : EventSystem<EntityAddedEvent>(only = Component1::class) {
        val list: MutableList<Entity> = ArrayList()
        override fun eventFired(entity: Entity, event: EntityAddedEvent) {
            list.add(event.entity)
        }
    }

    private inner class EntityRemovedWithConcreteComponentSystem : EventSystem<EntityRemovedEvent>(only = Component1::class) {
        val list: MutableList<Entity> = ArrayList()
        override fun eventFired(entity: Entity, event: EntityRemovedEvent) {
            list.add(event.entity)
        }
    }

    private val world = World()
    private val entity = world.createEntity()

    private val entityAddedSystem = EntityAddedSystem()
    private val entityRemovedSystem = EntityRemovedSystem()
    private val entityAddedWithConcreteComponentSystem = EntityAddedWithConcreteComponentSystem()
    private val entityRemovedWithConcreteComponentSystem = EntityRemovedWithConcreteComponentSystem()

    @BeforeAll
    fun registerSystems() {
        world.addSystem(entityAddedSystem)
        world.addSystem(entityRemovedSystem)
        world.addSystem(entityAddedWithConcreteComponentSystem)
        world.addSystem(entityRemovedWithConcreteComponentSystem)
    }

    @Test
    @Order(1)
    fun entityRemoved() {
        world.removeEntity(entity)
        world.act(0.0)
        assert(entityRemovedSystem.entity == entity)
    }

    @Test
    @Order(2)
    fun entityAdded() {
        world.addEntity(entity)
        world.act(0.0)
        assert(entityAddedSystem.entity == entity)
    }

    @Test
    @Order(3)
    fun entityWithComponentsWasNotAddedYet() {
        world.act(0.0)
        assert(entityAddedWithConcreteComponentSystem.list.isEmpty())
    }

    @Test
    @Order(4)
    fun entityWithComponentsWasNotRemovedYet() {
        world.act(0.0)
        assert(entityRemovedWithConcreteComponentSystem.list.isEmpty())
    }

    @Test
    @Order(5)
    fun entityWithWrongComponentAdded() {
        world.createEntity(Component2())
        world.act(0.0)
        assert(entityAddedWithConcreteComponentSystem.list.isEmpty())
    }

    @Test
    @Order(6)
    fun entityWithCorrectComponentAdded() {
        val entity = world.createEntity(Component1())
        world.act(0.0)
        assertContains(entityAddedWithConcreteComponentSystem.list, entity)
    }

    @Test
    @Order(7)
    fun entityWithCorrectComponentRemoved() {
        val entity = world.createEntity(Component1())
        world.removeEntity(entity)
        world.act(0.0)
        assertContains(entityRemovedWithConcreteComponentSystem.list, entity)
    }

}