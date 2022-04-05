package com.rdude.exECS

import com.rdude.exECS.component.Component
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class ComponentIdTest {

    private inner class TestComponent : Component

    @Test
    @Order(0)
    fun testStartId() {
        val startId = TestComponent().componentId
        assert(startId == 0)
    }

    @Test
    @Order(1)
    fun testIds1() {
        val startId = TestComponent().componentId
        TestComponent().componentId
        TestComponent().componentId
        TestComponent().componentId
        TestComponent().componentId
        val lastComponent = TestComponent()
        assert(startId + 5 == lastComponent.componentId)
    }

    @Test
    @Order(2)
    fun testIds2() {
        val startId = TestComponent().componentId
        TestComponent()
        TestComponent()
        TestComponent()
        TestComponent()
        val lastComponent = TestComponent()
        assert(startId + 1 == lastComponent.componentId)
    }


}