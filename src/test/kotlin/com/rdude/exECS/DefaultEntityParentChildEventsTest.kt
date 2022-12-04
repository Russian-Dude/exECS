package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.ChildEntityAddedEvent
import com.rdude.exECS.event.ChildEntityRemovedEvent
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
class DefaultEntityParentChildEventsTest : WorldAccessor() {

    private object TestComponent : Component

    private inner class ChildAddedSystem : EventSystem<ChildEntityAddedEvent>() {

        val parents = mutableSetOf<Entity>()
        val children = mutableSetOf<Entity>()

        override fun eventFired(event: ChildEntityAddedEvent) {
            parents += event.parentEntity
            children += event.childEntity
        }
    }

    private inner class ChildRemovedSystem : EventSystem<ChildEntityRemovedEvent>() {

        val parents = mutableSetOf<Entity>()
        val children = mutableSetOf<Entity>()

        override fun eventFired(event: ChildEntityRemovedEvent) {
            parents += event.parentEntity
            children += event.childEntity
        }
    }


    override val world = World()
    private val childAddedSystem = ChildAddedSystem()
    private val childRemovedSystem = ChildRemovedSystem()

    var parentEntity = Entity.NO_ENTITY
    var childEntity1 = Entity.NO_ENTITY
    var childEntity2 = Entity.NO_ENTITY


    @BeforeAll
    fun init() {
        world.registerSystem(childAddedSystem)
        world.registerSystem(childRemovedSystem)
    }

    @BeforeEach
    fun clearStoredEntities() {
        childAddedSystem.parents.clear()
        childAddedSystem.children.clear()
        childRemovedSystem.parents.clear()
        childRemovedSystem.children.clear()
    }


    @Test
    @Order(0)
    fun childrenAdded() {
        parentEntity = world.createEntity(TestComponent)
        childEntity1 = world.createEntity(TestComponent)
        childEntity2 = world.createEntity(TestComponent)
        parentEntity.addChild(childEntity1)
        parentEntity.addChild(childEntity2)
        world.act()
        assert(
            childAddedSystem.children.contains(childEntity1)
                    && childAddedSystem.children.contains(childEntity2)
                    && childAddedSystem.parents.contains(parentEntity)
                    && childRemovedSystem.parents.isEmpty()
                    && childRemovedSystem.children.isEmpty()
        )
    }

    @Test
    @Order(1)
    fun childRemovedDirectly() {
        childEntity1.removeFromParent()
        world.act()
        assert(
            childAddedSystem.children.isEmpty()
                    && childAddedSystem.parents.isEmpty()
                    && childRemovedSystem.parents.contains(parentEntity)
                    && childRemovedSystem.children.contains(childEntity1)
        )
    }

    @Test
    @Order(2)
    fun childRemovedWhenEntityRemoved() {
        childEntity2.remove()
        world.act()
        assert(
            childAddedSystem.children.isEmpty()
                    && childAddedSystem.parents.isEmpty()
                    && childRemovedSystem.parents.contains(parentEntity)
                    && childRemovedSystem.children.contains(childEntity2)
        )
    }

}