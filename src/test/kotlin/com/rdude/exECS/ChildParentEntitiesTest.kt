package com.rdude.exECS

import com.rdude.exECS.component.ChildEntityComponent
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ParentEntityComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ChildParentEntitiesTest : WorldAccessor() {

    private class TestComponent : Component

    private class EntityRemovedSystem : EventSystem<EntityRemovedEvent>() {

        val removedEntities = mutableListOf<Entity>()

        override fun eventFired(event: EntityRemovedEvent) {
            removedEntities.add(event.entity)
        }
    }

    override val world: World = World()

    private val entityRemovedSystem =
        EntityRemovedSystem().apply { this@ChildParentEntitiesTest.world.registerSystem(this) }

    private val parent1 = world.createEntity(TestComponent())
    private val parent2 = world.createEntity(TestComponent())
    private val child1 = world.createEntity(TestComponent())
    private val child2 = world.createEntity(TestComponent())

    @Test
    @Order(0)
    fun childInitialized() {
        parent1.addChild(child1)
        parent1.addChild(child2)
        assert(child1.parent == parent1 && child2.parent == parent1)
    }

    @Test
    @Order(1)
    fun parentInitialized() {
        assert(parent1.children.contains(child1) && parent1.children.contains(child2))
    }

    @Test
    @Order(2)
    fun childRemoved_parentProperty() {
        child1.removeFromParent()
        parent1.removeChild(child2)
        assert(child1.parent == Entity.NO_ENTITY && child2.parent == Entity.NO_ENTITY)
    }

    @Test
    @Order(3)
    fun childRemoved_childrenProperty() {
        assert(parent1.children.isEmpty())
    }

    @Test
    @Order(4)
    fun childrenRemovedWhenParentRemoved() {
        parent1.addChild(child1)
        parent1.addChild(child2)
        parent2.addChild(parent1)
        parent2.remove()
        world.act()
        assert(entityRemovedSystem.removedEntities.containsAll(listOf(parent2, parent1, child1, child2)))
    }

    @Test
    @Order(5)
    fun parentEntityComponentRemoved() {
        assert(!parent1.hasComponent<ParentEntityComponent>() && !parent2.hasComponent<ParentEntityComponent>())
    }

    @Test
    @Order(6)
    fun childEntityComponentsRemoved() {
        assert(!child1.hasComponent<ChildEntityComponent>() && !child2.hasComponent<ChildEntityComponent>() && !parent1.hasComponent<ChildEntityComponent>())
    }

}