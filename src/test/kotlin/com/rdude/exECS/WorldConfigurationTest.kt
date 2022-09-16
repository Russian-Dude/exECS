package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.Event
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorldConfigurationTest : WorldAccessor() {

    private class TestComponent : Component, Poolable

    private class TestEvent : Event, Poolable

    private class ComponentAddedSystem : EventSystem<ComponentAddedEvent<*>>() {

        var lastAddedComponent: Component? = null

        override fun eventFired(event: ComponentAddedEvent<*>) {
            lastAddedComponent = event.component
        }
    }

    private class ComponentRemovedSystem : EventSystem<ComponentRemovedEvent<*>>() {

        var lastRemovedComponent: Component? = null

        override fun eventFired(event: ComponentRemovedEvent<*>) {
            lastRemovedComponent = event.component
        }
    }

    override val world = World()
    private val config = world.configuration

    private val componentAddedSystem = ComponentAddedSystem()
    private val componentRemovedSystem = ComponentRemovedSystem()


    @BeforeAll
    fun registerSystems() {
        world.registerSystem(componentAddedSystem)
        world.registerSystem(componentRemovedSystem)
    }

    @BeforeEach
    fun clearSystems() {
        componentAddedSystem.lastAddedComponent = null
        componentRemovedSystem.lastRemovedComponent = null
    }


    @Test
    fun queueComponentAddedEventWhenEntityAddedDisabled() {
        config.queueComponentAddedWhenEntityAdded = false
        world.createEntity(fromPool<TestComponent>())
        world.act()
        assert(componentAddedSystem.lastAddedComponent == null)
    }

    @Test
    fun queueComponentAddedEventWhenEntityAddedEnabled() {
        config.queueComponentAddedWhenEntityAdded = true
        val component = fromPool<TestComponent>()
        world.createEntity(component)
        world.act()
        assert(componentAddedSystem.lastAddedComponent == component)
    }

    @Test
    fun queueComponentRemovedEventWhenEntityRemovedDisabled() {
        config.queueComponentRemovedWhenEntityRemoved = false
        val entity = world.createEntity(fromPool<TestComponent>())
        entity.remove()
        world.act()
        assert(componentRemovedSystem.lastRemovedComponent == null)
    }

    @Test
    fun queueComponentRemovedEventWhenEntityRemovedEnabled() {
        config.queueComponentRemovedWhenEntityRemoved = true
        val component = fromPool<TestComponent>()
        val entity = world.createEntity(component)
        entity.remove()
        world.act()
        world.act()
        assert(componentRemovedSystem.lastRemovedComponent == component)
    }

    @Test
    fun autoReturnPoolableEventsToPoolDisabled() {
        config.autoReturnPoolableEventsToPool = false
        val event = fromPool<TestEvent>()
        queueEvent(event)
        world.act()
        assert(!event.isInPool)
    }

    @Test
    fun autoReturnPoolableEventsToPoolEnabled() {
        config.autoReturnPoolableEventsToPool = true
        val event = fromPool<TestEvent>()
        queueEvent(event)
        world.act()
        assert(event.isInPool)
    }

    @Test
    fun autoReturnPoolableComponentsToPoolDisabled() {
        config.setAutoReturnPoolableComponentsToPool(false)
        val component = fromPool<TestComponent>()
        val entity = createEntity(component)
        entity.removeComponent<TestComponent>()
        world.act()
        assert(!component.isInPool)
    }

    @Test
    fun autoReturnPoolableComponentsToPoolEnabled() {
        config.setAutoReturnPoolableComponentsToPool(true)
        val component = fromPool<TestComponent>()
        val entity = createEntity(component)
        entity.removeComponent<TestComponent>()
        world.act()
        assert(component.isInPool)
    }


}