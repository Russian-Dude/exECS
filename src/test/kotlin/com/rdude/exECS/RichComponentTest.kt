package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.RichComponent
import com.rdude.exECS.entity.EntityUnoptimizedMethods
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class RichComponentTest {

    private class TestRichComponent : RichComponent

    private class SimpleTestComponent : Component

    private val world = World()

    private val richComponent = TestRichComponent()
    private val simpleComponent = SimpleTestComponent()

    @BeforeAll
    fun addEntities() {
        world.createEntity(richComponent, simpleComponent)
        world.createEntity(richComponent)
        world.act(0.0)
    }

    @Test
    @Order(0)
    fun testSize() {
        assert(richComponent.insideEntitiesSet.size == 2)
    }

    @Test
    @Order(1)
    fun getAnotherComponent() {
        val list = ArrayList<Component>()
        richComponent.insideEntitiesSet.forEach {
            val component = EntityUnoptimizedMethods.getComponent<SimpleTestComponent>(it, world)
            component?.apply { list.add(this) }
        }
        assert(list.size == 1 && list.first() == simpleComponent)
    }

    @Test
    @Order(2)
    fun removeComponent() {
        EntityUnoptimizedMethods.removeComponent<TestRichComponent>(richComponent.insideEntitiesSet.first(), world)
        world.act(0.0)
        println(richComponent.insideEntitiesSet.size)
        assert(richComponent.insideEntitiesSet.size == 1)
    }

    @Test
    @Order(3)
    fun removeEntity() {
        EntityUnoptimizedMethods.remove(richComponent.insideEntitiesSet.first(), world)
        world.act(0.0)
        assert(richComponent.insideEntitiesSet.size == 0)
    }

}