package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.value.IntComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityBlueprint
import com.rdude.exECS.entity.EntityBlueprintConfiguration
import com.rdude.exECS.event.EntityAddedEvent
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityBlueprintTest : WorldAccessor() {

    private class TestComponent : Component

    private class TestPoolableComponent : Component, Poolable

    private class TestIntComponent : IntComponent(), Poolable {
        override fun reset() {
            value = 0
        }
    }

    private class BPIntConfig(var value: Int = 0) : EntityBlueprintConfiguration(), Poolable {
        override fun reset() {
            value = 0
        }
    }

    private class EntityAddedSystem : EventSystem<EntityAddedEvent>() {

        val addedEntities = mutableListOf<Entity>()

        override fun eventFired(event: EntityAddedEvent) {
            addedEntities.add(event.entity)
        }
    }


    private val onlyComponentsBP = EntityBlueprint {
        withComponent(TestComponent())
        withComponent<TestPoolableComponent>()
    }

    private val onlyComponentsWithConfigBP = EntityBlueprint<BPIntConfig> {
        withComponent<TestIntComponent> { value = it.value }
    }

    private val withChildrenNoConfigBP = EntityBlueprint {
        withComponent<TestPoolableComponent>()
        onlyComponentsBP()
    }

    private val withChildrenWithSimpleConfigNoConfigBP = EntityBlueprint {
        withComponent(TestComponent())
        onlyComponentsWithConfigBP.withConfig { value = 717 }
    }

    private val withChildrenWithSimpleComponentNoConfigBP = EntityBlueprint {
        withComponent(TestComponent())
        onlyComponentsWithConfigBP.withComponent<TestIntComponent> { value = 717 }
    }

    private val withChildrenWithSimpleConfigWithConfig = EntityBlueprint<BPIntConfig> {
        withComponent<TestPoolableComponent>()
        onlyComponentsWithConfigBP.withConfig { value = it.value }
    }

    private val withChildrenWithSimpleComponentWithConfig = EntityBlueprint<BPIntConfig> {
        withComponent<TestPoolableComponent>()
        onlyComponentsWithConfigBP.withComponent<TestIntComponent> { value = it.value }
    }

    private val withNestedChildren = EntityBlueprint {
        withComponent(TestComponent())
        onlyComponentsBP {
            onlyComponentsBP()
            withChildrenNoConfigBP {
                withChildrenWithSimpleConfigWithConfig.withConfig { value = 717 }
            }
        }
    }

    private val nestedWithComponent = EntityBlueprint {
        withComponent(TestComponent())
        onlyComponentsBP {
            onlyComponentsWithConfigBP {
                withComponent<TestIntComponent> { value = 999 }
            }
        }
    }

    private val withDynamicChildrenAmount = EntityBlueprint<BPIntConfig> {
        withComponent<TestPoolableComponent>()
        for (i in 0..it.value) {
            onlyComponentsBP()
        }
    }


    override val world = World()

    private val entityAddedSystem = EntityAddedSystem().apply { this@EntityBlueprintTest.world.registerSystem(this) }

    @BeforeEach
    fun clearSystem() {
        entityAddedSystem.addedEntities.clear()
    }

    @Test
    fun onlyComponents() {
        createEntity(onlyComponentsBP)
        world.act()
        val addedEntity = entityAddedSystem.addedEntities.first()
        assert(addedEntity.hasComponent<TestComponent>() && addedEntity.hasComponent<TestPoolableComponent>())
    }

    @Test
    fun onlyComponentsWithConfig() {
        createEntity(onlyComponentsWithConfigBP) { value = 123 }
        world.act()
        assert(entityAddedSystem.addedEntities.first().getComponent<TestIntComponent>()?.value == 123)
    }

    @Test
    fun withChildrenNoConfig() {
        val entity = createEntity(withChildrenNoConfigBP)
        world.act()
        val hasComponent = entity.hasComponent<TestPoolableComponent>()
        val child = entity.children.first()
        val childHasComponents = child.hasComponent<TestComponent>() && child.hasComponent<TestPoolableComponent>()
        val bothAdded = entityAddedSystem.addedEntities.containsAll(listOf(entity, child))
        assert(hasComponent && childHasComponents && bothAdded)
    }

    @Test
    fun withChildrenWithSimpleConfigNoMainConfig() {
        val entity = createEntity(withChildrenWithSimpleConfigNoConfigBP)
        world.act()
        val hasComponent = entity.hasComponent<TestComponent>()
        val child = entity.children.first()
        val configuredChildComponentValueCorrect = child<TestIntComponent>()?.value == 717
        val bothAdded = entityAddedSystem.addedEntities.containsAll(listOf(entity, child))
        assert(hasComponent && configuredChildComponentValueCorrect && bothAdded)
    }

    @Test
    fun withChildrenWithSimpleComponentNoMainConfigBP() {
        val entity = createEntity(withChildrenWithSimpleComponentNoConfigBP)
        world.act()
        val hasComponent = entity.hasComponent<TestComponent>()
        val child = entity.children.first()
        val configuredChildComponentValueCorrect = child<TestIntComponent>()?.value == 717
        val bothAdded = entityAddedSystem.addedEntities.containsAll(listOf(entity, child))
        assert(hasComponent && configuredChildComponentValueCorrect && bothAdded)
    }

    @Test
    fun withChildrenWithSimpleConfigWithMainConfig() {
        val entity = createEntity(withChildrenWithSimpleConfigWithConfig) { value = 321 }
        world.act()
        val hasComponent = entity.hasComponent<TestPoolableComponent>()
        val child = entity.children.first()
        val configuredChildComponentValueCorrect = child<TestIntComponent>()?.value == 321
        val bothAdded = entityAddedSystem.addedEntities.containsAll(listOf(entity, child))
        assert(hasComponent && configuredChildComponentValueCorrect && bothAdded)
    }

    @Test
    fun withChildrenWithSimpleComponentWithMainConfig() {
        val entity = createEntity(withChildrenWithSimpleComponentWithConfig) { value = 321 }
        world.act()
        val hasComponent = entity.hasComponent<TestPoolableComponent>()
        val child = entity.children.first()
        val configuredChildComponentValueCorrect = child<TestIntComponent>()?.value == 321
        val bothAdded = entityAddedSystem.addedEntities.containsAll(listOf(entity, child))
        assert(hasComponent && configuredChildComponentValueCorrect && bothAdded)
    }

    @Test
    fun withNestedChildren() {
        val entity = createEntity(withNestedChildren)
        world.act()
        val mainHasComponent = entity.hasComponent<TestComponent>()
        val mainChild = entity.children.first()
        val mainChildHasBothChildren = mainChild.children.size == 2
        val simpleChild = mainChild.children.find { it.children.size == 0 }
        val simpleChildHasComponents = simpleChild.hasComponent<TestComponent>() && simpleChild.hasComponent<TestPoolableComponent>()
        val complexChild = mainChild.children.find { it.children.size == 2 }
        val lastChild = complexChild.children.find { it.children.size == 1 }
        val lastChildCorrectComponentValue = lastChild.children.first().getComponent<TestIntComponent>()?.value == 717
        assert(mainHasComponent && mainChildHasBothChildren && simpleChildHasComponents && lastChildCorrectComponentValue)
    }

    @Test
    fun nestedWithComponent() {
        val entity = createEntity(nestedWithComponent)
        val mainHasComponent = entity.hasComponent<TestComponent>()
        val mainChild = entity.children.first()
        val lastChild = mainChild.children.first()
        val lastChildCorrectComponentValue = lastChild.getComponent<TestIntComponent>()?.value == 999
        assert(mainHasComponent && lastChildCorrectComponentValue)
    }

    @Test
    fun withDynamicChildrenAmount() {
        val entity = createEntity(withDynamicChildrenAmount) { value = 15 }
        world.act()
        assert(entity.children.size == 16 && entity.children.first().hasComponent<TestComponent>())
    }

}