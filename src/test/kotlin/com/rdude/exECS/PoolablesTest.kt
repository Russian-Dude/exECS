package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.Event
import com.rdude.exECS.pool.ConstructorForDefaultPool
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.full.findAnnotation

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PoolablesTest {

    class SimplePoolable : Poolable

    class SimplePoolableEvent : Event, Poolable

    class SimplePoolableComponent : Component, Poolable

    private inner class EntityRemoverSystem : ActingSystem(only = SimplePoolableComponent::class) {
        override fun act(entity: EntityWrapper, delta: Double) {
            entity.remove()
        }
    }

    private inner class ComponentRemoverSystem : ActingSystem(only = SimplePoolableComponent::class) {
        override fun act(entity: EntityWrapper, delta: Double) {
            entity.removeComponent<SimplePoolableComponent>()
        }
    }

    private abstract class TestConstructorsPoolable : Poolable {
        var check = false
    }

    private class PoolableWithPrimaryConstructor(some: Int = 5) : TestConstructorsPoolable() {
        constructor() : this(5) { check = !check }
        init { check = !check }
    }

    private class PoolableWithAnyConstructor(some: Int) : TestConstructorsPoolable() {
        constructor() : this(5) { check = true }
    }

    private class PoolableWithPreferredConstructor(some: Int = 5) : TestConstructorsPoolable() {

        constructor() : this(5)

        @ConstructorForDefaultPool
        constructor(some: String = "") : this() { check = true }

        constructor(some: String = "", some2: Int) : this()
    }

    @Test
    fun manualObtainAndReturn() {
        val pool = Pool { SimplePoolable() }
        val obtainFirst = pool.obtain()
        val obtainSecond = pool.obtain()
        obtainFirst.returnToPool()
        val obtainFirstAgain = pool.obtain()
        assert(obtainFirst !== obtainSecond && obtainFirst === obtainFirstAgain)
    }

    @Test
    fun manualObtainAndReturnFromDefaultPools() {
        val obtainFirst = fromPool<SimplePoolable>()
        val obtainSecond = fromPool<SimplePoolable>()
        obtainFirst.returnToPool()
        val obtainFirstAgain = fromPool<SimplePoolable>()
        assert(obtainFirst !== obtainSecond && obtainFirst === obtainFirstAgain)
    }

    @Test
    fun returnEventToPoolAfterBeingFired() {
        val world = World()
        val event = fromPool<SimplePoolableEvent>()
        event.returnToPool()
        world.queueEvent<SimplePoolableEvent>()
        world.act(0.0)
        val afterAct = fromPool<SimplePoolableEvent>()
        assert(event === afterAct)
    }

    @Test
    fun returnComponentToPoolAfterEntityRemoved() {
        val world = World()
        val system = EntityRemoverSystem()
        world.addSystem(system)
        val component = fromPool<SimplePoolableComponent>()
        world.createEntity(component)
        world.act(0.0)
        world.act(0.0)
        val component2 = fromPool<SimplePoolableComponent>()
        assert(component === component2)
    }

    @Test
    fun returnComponentToPoolAfterBeingRemoved() {
        val world = World()
        val system = ComponentRemoverSystem()
        world.addSystem(system)
        val component = fromPool<SimplePoolableComponent>()
        world.createEntity(component)
        world.act(0.0)
        world.act(0.0)
        val component2 = fromPool<SimplePoolableComponent>()
        assert(component === component2)
    }

    @Test
    fun fromDefaultPoolAnnotatedConstructor() {
        assert(fromPool<PoolableWithPreferredConstructor>().check)
    }

    @Test
    fun fromDefaultPoolAnyConstructor() {
        assert(fromPool<PoolableWithAnyConstructor>().check)
    }

    @Test
    fun fromDefaultPoolPrimaryConstructor() {
        assert(fromPool<PoolableWithPrimaryConstructor>().check)
    }
}