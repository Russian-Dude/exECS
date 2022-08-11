package com.rdude.exECS

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.RichComponent
import com.rdude.exECS.component.observable.ObservableIntComponent
import com.rdude.exECS.component.observable.ObservableValueComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.ComponentChangedEvent
import com.rdude.exECS.event.change
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ObservableComponentTest {

    private enum class TestEnum { BEFORE, AFTER }

    private class TestChange : ComponentChange

    private class TestObservableComponent : ObservableComponent<TestChange>

    private class TestObservableIntComponent : ObservableIntComponent()

    private class TestObservableValueComponent : ObservableValueComponent<TestEnum>(TestEnum.BEFORE)

    private class TestObservableNullableValueComponent : ObservableValueComponent<TestEnum?>(null)

    private class TestRichObservableValueComponent : ObservableValueComponent<TestEnum?>(null), RichComponent


    private class TestObservableSystem : EventSystem<ComponentChangedEvent<TestObservableComponent>>() {
        var fired = false
        override fun eventFired(event: ComponentChangedEvent<TestObservableComponent>) {
            fired = true
        }
    }

    private class TestIntObservableSystem : EventSystem<ComponentChangedEvent<TestObservableIntComponent>>() {
        var value = Int.MIN_VALUE
        override fun eventFired(event: ComponentChangedEvent<TestObservableIntComponent>) {
            value = event.change.newValue
        }
    }

    private class TestValueObservableSystem : EventSystem<ComponentChangedEvent<TestObservableValueComponent>>() {
        lateinit var value: TestEnum
        override fun eventFired(event: ComponentChangedEvent<TestObservableValueComponent>) {
            value = event.change.newValue
        }
    }

    private class TestNullableValueObservableSystem : EventSystem<ComponentChangedEvent<TestObservableNullableValueComponent>>() {
        var value: TestEnum? = TestEnum.BEFORE
        override fun eventFired(event: ComponentChangedEvent<TestObservableNullableValueComponent>) {
            value = event.change.newValue
        }
    }

    private class TestConditionSystem : IterableActingSystem(only = TestRichObservableValueComponent::class { value == TestEnum.AFTER }) {
        var fired = false
        override fun act(entity: Entity) {
            fired = true
        }
    }


    private val world = World()

    private val testObservableSystem = TestObservableSystem()
    private val testIntObservableSystem = TestIntObservableSystem()
    private val testValueObservableSystem = TestValueObservableSystem()
    private val testNullableValueObservableSystem = TestNullableValueObservableSystem()
    private val testConditionSystem = TestConditionSystem()

    private val testObservableComponent = TestObservableComponent()
    private val testObservableIntComponent = TestObservableIntComponent()
    private val testObservableValueComponent = TestObservableValueComponent()
    private val testObservableNullableValueComponent = TestObservableNullableValueComponent()
    private val testRichObservableValueComponent = TestRichObservableValueComponent()


    @BeforeAll
    fun registerSystemsAndAddComponents() {
        world.registerSystem(testObservableSystem)
        world.registerSystem(testIntObservableSystem)
        world.registerSystem(testValueObservableSystem)
        world.registerSystem(testNullableValueObservableSystem)
        world.registerSystem(testConditionSystem)

        world.createEntity(
            testObservableComponent,
            testObservableIntComponent,
            testObservableValueComponent,
            testObservableNullableValueComponent,
            testRichObservableValueComponent
        )

        world.act()
    }


    @Test
    fun simpleObservable() {
        testObservableComponent.componentChanged(TestChange())
        world.act()
        assert(testObservableSystem.fired)
    }

    @Test
    fun intObservable() {
        testObservableIntComponent.value = 717
        world.act()
        assert(testIntObservableSystem.value == 717)
    }

    @Test
    fun valueObservable() {
        testObservableValueComponent.value = TestEnum.AFTER
        world.act()
        assert(testValueObservableSystem.value == TestEnum.AFTER)
    }

    @Test
    fun nullableValueObservable1() {
        testObservableNullableValueComponent.value = TestEnum.AFTER
        world.act()
        assert(testNullableValueObservableSystem.value == TestEnum.AFTER)
    }

    @Test
    fun nullableValueObservable2() {
        testObservableNullableValueComponent.value = null
        world.act()
        assert(testNullableValueObservableSystem.value == null)
    }

    @Test
    fun condition1() {
        testConditionSystem.fired = false
        testRichObservableValueComponent.value = TestEnum.BEFORE
        world.act()
        assert(!testConditionSystem.fired)
    }

    @Test
    fun condition2() {
        testConditionSystem.fired = false
        testRichObservableValueComponent.value = TestEnum.AFTER
        world.act()
        assert(testConditionSystem.fired)
    }



}