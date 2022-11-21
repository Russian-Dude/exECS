package com.rdude.exECS

import com.rdude.exECS.component.ImmutableComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation::class)
class ImmutableComponentTest {

    private inner class TestImmutableComponent(val value: Int = 0) : ImmutableComponent

    private val immutableComponent = TestImmutableComponent()

    private inner class System1 : IterableActingSystem(only = immutableComponent) {
        var immutableComponent: ImmutableComponent? = null
        override fun act(entity: Entity) {
            immutableComponent = entity.getComponent<TestImmutableComponent>()
        }
    }

    private inner class System2 : IterableActingSystem(only = TestImmutableComponent::class) {
        var immutableComponent: ImmutableComponent? = null
        override fun act(entity: Entity) {
            immutableComponent = entity.getComponent<TestImmutableComponent>()
        }
    }

    private inner class System3 : IterableActingSystem(only = TestImmutableComponent::class { value > 10 }) {
        var immutableComponent: ImmutableComponent? = null
        override fun act(entity: Entity) {
            immutableComponent = entity.getComponent<TestImmutableComponent>()
        }
    }

    private lateinit var world: World
    private lateinit var system1: System1
    private lateinit var system2: System2
    private lateinit var system3: System3

    @BeforeAll
    fun init() {
        world = World()
        system1 = System1()
        system2 = System2()
        system3 = System3()
        world.registerSystem(system1)
        world.registerSystem(system2)
        world.registerSystem(system3)
        world.createEntity(immutableComponent)
        world.act()
    }

    @Test
    @Order(0)
    fun subscribedAsInstance() {
        assert(immutableComponent == system1.immutableComponent)
    }

    @Test
    @Order(1)
    fun subscribedAsComponent() {
        assert(immutableComponent == system2.immutableComponent)
    }

    @Test
    @Order(2)
    fun subscribedByCondition() {
        assert(system3.immutableComponent == null)
    }

    @Test
    @Order(3)
    fun subscribeByCondition2() {
        val component = TestImmutableComponent(717)
        world.createEntity(component)
        world.act()
        assert(system3.immutableComponent == component)
    }



}