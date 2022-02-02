package com.rdude.exECS

import com.rdude.exECS.aspect.and
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.system.ActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AspectTest {

    private inner class Component1 : Component
    private inner class Component2 : Component
    private inner class Component3 : Component
    private inner class Component4 : Component
    private inner class Component5 : Component

    private inner class AllOf123 : ActingSystem(allOf = Component1::class and Component2::class and Component3::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class AllOf123Exclude4 : ActingSystem(
        allOf = Component1::class and Component2::class and Component3::class,
        exclude = Component4::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class Any123 : ActingSystem(anyOf = Component1::class and Component2::class and Component3::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class AnyOf123Exclude4 : ActingSystem(
        anyOf = Component1::class and Component2::class and Component3::class,
        exclude = Component4::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class AllOf12AnyOf34 : ActingSystem(
        allOf = Component1::class and Component2::class,
        anyOf = Component3::class and Component4::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class AllOf12AnyOf34Exclude5 : ActingSystem(
        allOf = Component1::class and Component2::class,
        anyOf = Component3::class and Component4::class,
        exclude = Component5::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class Only1 : ActingSystem(only = Component1::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class Only1Exclude2 : ActingSystem(only = Component1::class, exclude = Component2::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class AllOf1AnyOf23 : ActingSystem(
        allOf = Component1::class,
        anyOf = Component2::class and Component3::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class AllOf1AnyOf23Exclude4 : ActingSystem(
        allOf = Component1::class,
        anyOf = Component2::class and Component3::class,
        exclude = Component4::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private inner class AllOf1AnyOf23Exclude45 : ActingSystem(
        allOf = Component1::class,
        anyOf = Component2::class and Component3::class,
        exclude = Component4::class and Component5::class) {
        var fired = false
        override fun act(entity: EntityWrapper, delta: Double) {
            fired = true
        }
    }

    private val world = World()

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
        world.addSystem(allOf123)
        world.addSystem(allOf123Exclude4)
        world.addSystem(anyOf123)
        world.addSystem(anyOf123Exclude4)
        world.addSystem(allOf12AnyOf34)
        world.addSystem(allOf12AnyOf34Exclude5)
        world.addSystem(only1)
        world.addSystem(only1Exclude2)
        world.addSystem(allOf1AnyOf23)
        world.addSystem(allOf1AnyOf23Exclude4)
        world.addSystem(allOf1AnyOf23Exclude45)
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
    }

    @BeforeEach
    fun clearEntities() {
        world.clearEntities()
    }

    @Test
    fun allOf1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act(0.0)
        assert(allOf123.fired)
    }

    @Test
    fun allOf2() {
        world.createEntity(Component2(), Component1(), Component3(), Component4(), Component5())
        world.act(0.0)
        assert(allOf123.fired)
    }

    @Test
    fun allOf3() {
        world.createEntity(Component1(), Component2())
        world.act(0.0)
        assertFalse(allOf123.fired)
    }

    @Test
    fun allOf4() {
        world.createEntity(Component5())
        world.act(0.0)
        assertFalse(allOf123.fired)
    }

    @Test
    fun allOfWithExclude1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act(0.0)
        assert(allOf123Exclude4.fired)
    }

    @Test
    fun allOfWithExclude2() {
        world.createEntity(Component1(), Component2(), Component3(), Component4())
        world.act(0.0)
        assertFalse(allOf123Exclude4.fired)
    }

    @Test
    fun anyOf1() {
        world.createEntity(Component1())
        world.act(0.0)
        assert(anyOf123.fired)
    }

    @Test
    fun anyOf2() {
        world.createEntity(Component2(), Component3())
        world.act(0.0)
        assert(anyOf123.fired)
    }

    @Test
    fun anyOf3() {
        world.createEntity(Component5())
        world.act(0.0)
        assertFalse(anyOf123.fired)
    }

    @Test
    fun anyOfWithExclude1() {
        world.createEntity(Component2())
        world.act(0.0)
        assert(anyOf123Exclude4.fired)
    }

    @Test
    fun anyOfWithExclude2() {
        world.createEntity(Component2(), Component4())
        world.act(0.0)
        assertFalse(anyOf123Exclude4.fired)
    }

    @Test
    fun allOfAnyOf1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act(0.0)
        assert(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf2() {
        world.createEntity(Component1(), Component2(), Component3(), Component4())
        world.act(0.0)
        assert(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf3() {
        world.createEntity(Component1(), Component2())
        world.act(0.0)
        assertFalse(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf4() {
        world.createEntity(Component1(), Component3())
        world.act(0.0)
        assertFalse(allOf12AnyOf34.fired)
    }

    @Test
    fun allOfAnyOf5() {
        world.createEntity(Component1(), Component3())
        world.act(0.0)
        assert(allOf1AnyOf23.fired)
    }

    @Test
    fun allOfAnyOf6() {
        world.createEntity(Component1())
        world.act(0.0)
        assertFalse(allOf1AnyOf23.fired)
    }

    @Test
    fun allOfAnyOf7() {
        world.createEntity(Component2())
        world.act(0.0)
        assertFalse(allOf1AnyOf23.fired)
    }

    @Test
    fun allOfAnyOfWithExclude1() {
        world.createEntity(Component1(), Component2(), Component3())
        world.act(0.0)
        assert(allOf12AnyOf34Exclude5.fired)
    }

    @Test
    fun allOfAnyOfWithExclude2() {
        world.createEntity(Component1(), Component2(), Component4(), Component5())
        world.act(0.0)
        assertFalse(allOf12AnyOf34Exclude5.fired)
    }

    @Test
    fun allOfAnyOfWithExclude3() {
        world.createEntity(Component1(), Component2(), Component4(), Component5())
        world.act(0.0)
        assertFalse(allOf1AnyOf23Exclude4.fired)
    }

    @Test
    fun allOfAnyOfWithExclude4() {
        world.createEntity(Component1(), Component2(), Component5())
        world.act(0.0)
        assert(allOf1AnyOf23Exclude4.fired)
    }

    @Test
    fun allOfAnyOfWithExclude5() {
        world.createEntity(Component1(), Component3(), Component5())
        world.act(0.0)
        assertFalse(allOf1AnyOf23Exclude45.fired)
    }

    @Test
    fun only1() {
        world.createEntity(Component1())
        world.act(0.0)
        assert(only1.fired)
    }

    @Test
    fun only2() {
        world.createEntity(Component1(), Component2())
        world.act(0.0)
        assert(only1.fired)
    }

    @Test
    fun only3() {
        world.createEntity(Component2())
        world.act(0.0)
        assertFalse(only1.fired)
    }

    @Test
    fun onlyWithExclude1() {
        world.createEntity(Component1())
        world.act(0.0)
        assert(only1Exclude2.fired)
    }

    @Test
    fun onlyWithExclude2() {
        world.createEntity(Component1(), Component2())
        world.act(0.0)
        assertFalse(only1Exclude2.fired)
    }


}