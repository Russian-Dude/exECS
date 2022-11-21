package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ImmutableComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.exception.AspectNotCorrectException
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.*
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AspectTest {

    private inner class Component1 : Component
    private inner class Component2 : Component
    private inner class Component3 : Component
    private inner class Component4 : Component
    private inner class Component5 : Component
    private inner class ImmutableComponent1 : ImmutableComponent

    private inner class AllOf123 : IterableActingSystem(allOf = Component1::class and Component2::class and Component3::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class AllOf123Exclude4 : IterableActingSystem(
        allOf = Component1::class and Component2::class and Component3::class,
        exclude = Component4::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class Any123 : IterableActingSystem(anyOf = Component1::class and Component2::class and Component3::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class AnyOf123Exclude4 : IterableActingSystem(
        anyOf = Component1::class and Component2::class and Component3::class,
        exclude = Component4::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class AllOf12AnyOf34 : IterableActingSystem(
        allOf = Component1::class and Component2::class,
        anyOf = Component3::class and Component4::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class AllOf12AnyOf34Exclude5 : IterableActingSystem(
        allOf = Component1::class and Component2::class,
        anyOf = Component3::class and Component4::class,
        exclude = Component5::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class Only1 : IterableActingSystem(only = Component1::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class Only1Exclude2 : IterableActingSystem(only = Component1::class, exclude = Component2::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class AllOf1AnyOf23 : IterableActingSystem(
        allOf = Component1::class,
        anyOf = Component2::class and Component3::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class AllOf1AnyOf23Exclude4 : IterableActingSystem(
        allOf = Component1::class,
        anyOf = Component2::class and Component3::class,
        exclude = Component4::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class AllOf1AnyOf23Exclude45 : IterableActingSystem(
        allOf = Component1::class,
        anyOf = Component2::class and Component3::class,
        exclude = Component4::class and Component5::class) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }

    private inner class WrongSystem1 : IterableActingSystem(anyOf = Component1::class and Component2::class and Component1::class) {
        override fun act(entity: Entity) { }
    }

    private inner class WrongSystem2 : IterableActingSystem(
        allOf = Component1::class and Component2::class,
        exclude = Component1::class
    ) {
        override fun act(entity: Entity) { }
    }

    private inner class WrongSystem3 : IterableActingSystem(
        allOf = state1 and state2,
        exclude = ImmutableComponent1::class
    ) {
        override fun act(entity: Entity) { }
    }

    private val world = World()

    private val state1 = ImmutableComponent1()
    private val state2 = ImmutableComponent1()

    private val allOf123 = AllOf123()
    private val anyOf123 = Any123()
    private val allOf123Exclude4 = AllOf123Exclude4()
    private val anyOf123Exclude4 = AnyOf123Exclude4()
    private val allOf12AnyOf34 = AllOf12AnyOf34()
    private val allOf12AnyOf34Exclude5 = AllOf12AnyOf34Exclude5()
    private val only1 = Only1()
    private val only1Exclude2 = Only1Exclude2()
    private val allOf1AnyOf23 = AllOf1AnyOf23()
    private val allOf1AnyOf23Exclude4 = AllOf1AnyOf23Exclude4()
    private val allOf1AnyOf23Exclude45 = AllOf1AnyOf23Exclude45()

    @BeforeAll
    fun registerSystems() {
        world.registerSystem(allOf123)
        world.registerSystem(allOf123Exclude4)
        world.registerSystem(anyOf123)
        world.registerSystem(anyOf123Exclude4)
        world.registerSystem(allOf12AnyOf34)
        world.registerSystem(allOf12AnyOf34Exclude5)
        world.registerSystem(only1)
        world.registerSystem(only1Exclude2)
        world.registerSystem(allOf1AnyOf23)
        world.registerSystem(allOf1AnyOf23Exclude4)
        world.registerSystem(allOf1AnyOf23Exclude45)
    }

    @BeforeEach
    fun resetFiredFlagOnAllSystems() {
        allOf123.fired = false
        allOf123Exclude4.fired = false
        anyOf123.fired = false
        anyOf123Exclude4.fired = false
        allOf12AnyOf34.fired = false
        allOf12AnyOf34Exclude5.fired = false
        only1.fired = false
        only1Exclude2.fired = false
        allOf1AnyOf23.fired = false
        allOf1AnyOf23Exclude4.fired = false
        allOf1AnyOf23Exclude45.fired = false
        world.clearEntities()
    }

    @BeforeEach
    fun clearEntities() {
        world.clearEntities()
    }

    @Test
    fun allOf1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act()
        assert(allOf123.fired)
    }

    @Test
    fun allOf2() {
        world.createEntity(Component2(), Component1(), Component3(), Component4(), Component5())
        world.act()
        assert(allOf123.fired)
    }

    @Test
    fun allOf3() {
        world.createEntity(Component1(), Component2())
        world.act()
        assertFalse(allOf123.fired)
    }

    @Test
    fun allOf4() {
        world.createEntity(Component5())
        world.act()
        assertFalse(allOf123.fired)
    }

    @Test
    fun allOfWithExclude1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act()
        assert(allOf123Exclude4.fired)
    }

    @Test
    fun allOfWithExclude2() {
        world.createEntity(Component1(), Component2(), Component3(), Component4())
        world.act()
        assertFalse(allOf123Exclude4.fired)
    }

    @Test
    fun anyOf1() {
        world.createEntity(Component1())
        world.act()
        assert(anyOf123.fired)
    }

    @Test
    fun anyOf2() {
        world.createEntity(Component2(), Component3())
        world.act()
        assert(anyOf123.fired)
    }

    @Test
    fun anyOf3() {
        world.createEntity(Component5())
        world.act()
        assertFalse(anyOf123.fired)
    }

    @Test
    fun anyOfWithExclude1() {
        world.createEntity(Component2())
        world.act()
        assert(anyOf123Exclude4.fired)
    }

    @Test
    fun anyOfWithExclude2() {
        world.createEntity(Component2(), Component4())
        world.act()
        assertFalse(anyOf123Exclude4.fired)
    }

    @Test
    fun allOfAnyOf1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act()
        assert(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf2() {
        world.createEntity(Component1(), Component2(), Component3(), Component4())
        world.act()
        assert(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf3() {
        world.createEntity(Component1(), Component2())
        world.act()
        assertFalse(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf4() {
        world.createEntity(Component1(), Component3())
        world.act()
        assertFalse(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf5() {
        world.createEntity(Component1(), Component3())
        world.act()
        assert(allOf1AnyOf23.fired)
    }

    @Test
    fun allOfAnyOf6() {
        world.createEntity(Component1())
        world.act()
        assertFalse(allOf1AnyOf23.fired)
    }

    @Test
    fun allOfAnyOf7() {
        world.createEntity(Component2())
        world.act()
        assertFalse(allOf1AnyOf23.fired)
    }

    @Test
    fun allOfAnyOfWithExclude1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act()
        assert(allOf12AnyOf34Exclude5.fired)
    }

    @Test
    fun allOfAnyOfWithExclude2() {
        world.createEntity(Component1(), Component2(), Component4(), Component5())
        world.act()
        assertFalse(allOf12AnyOf34Exclude5.fired)
    }

    @Test
    fun allOfAnyOfWithExclude3() {
        world.createEntity(Component1(), Component2(), Component4(), Component5())
        world.act()
        assertFalse(allOf1AnyOf23Exclude4.fired)
    }

    @Test
    fun allOfAnyOfWithExclude4() {
        world.createEntity(Component1(), Component2(), Component5())
        world.act()
        assert(allOf1AnyOf23Exclude4.fired)
    }

    @Test
    fun allOfAnyOfWithExclude5() {
        world.createEntity(Component1(), Component3(), Component5())
        world.act()
        assertFalse(allOf1AnyOf23Exclude45.fired)
    }

    @Test
    fun only1() {
        world.createEntity(Component1())
        world.act()
        assert(only1.fired)
    }

    @Test
    fun only2() {
        world.createEntity(Component1(), Component2())
        world.act()
        assert(only1.fired)
    }

    @Test
    fun only3() {
        world.createEntity(Component2())
        world.act()
        assertFalse(only1.fired)
    }

    @Test
    fun onlyWithExclude1() {
        world.createEntity(Component1())
        world.act()
        assert(only1Exclude2.fired)
    }

    @Test
    fun onlyWithExclude2() {
        world.createEntity(Component1(), Component2())
        world.act()
        assertFalse(only1Exclude2.fired)
    }

    @Test
    fun wrongAspect1() {
        assertThrows<AspectNotCorrectException> { WrongSystem1() }
    }

    @Test
    fun wrongAspect2() {
        assertThrows<AspectNotCorrectException> { WrongSystem2() }
    }

    @Test
    fun wrongAspect3() {
        assertThrows<AspectNotCorrectException> { WrongSystem3() }
    }


}