package com.rdude.exECS

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ImmutableComponent
import com.rdude.exECS.component.UniqueComponent
import com.rdude.exECS.component.observable.ObservableIntComponent
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityOrder
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.world.World
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityOrderTest {

    private object MustHaveComponent : Component

    private object TagComponent : Component

    private inner class ImmutableIntComponent(val value: Int) : ImmutableComponent, Comparable<ImmutableIntComponent> {
        override fun compareTo(other: ImmutableIntComponent): Int = value.compareTo(other.value)
    }

    private inner class UniqueObservableIntComponent(value: Int) : ObservableIntComponent(value), UniqueComponent


    private inner class AscendingNullsFirstImmutableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = ImmutableIntComponent::class.ascending(true)
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<ImmutableIntComponent>()?.value
        }
    }


    private inner class AscendingNullsLastImmutableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = ImmutableIntComponent::class.ascending()
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<ImmutableIntComponent>()?.value
        }
    }


    private inner class DescendingNullsFirstImmutableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = ImmutableIntComponent::class.descending(true)
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<ImmutableIntComponent>()?.value
        }
    }


    private inner class DescendingNullsLastImmutableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = ImmutableIntComponent::class.descending()
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<ImmutableIntComponent>()?.value
        }
    }


    private inner class AscendingNullsFirstObservableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = UniqueObservableIntComponent::class.ascending(true)
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<UniqueObservableIntComponent>()?.value
        }
    }


    private inner class AscendingNullsLastObservableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = UniqueObservableIntComponent::class.ascending()
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<UniqueObservableIntComponent>()?.value
        }
    }


    private inner class DescendingNullsFirstObservableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = UniqueObservableIntComponent::class.descending(true)
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<UniqueObservableIntComponent>()?.value
        }
    }


    private inner class DescendingNullsLastObservableComponentSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = UniqueObservableIntComponent::class.descending()
    ) {

        val iteratedValues = ArrayList<Int?>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity<UniqueObservableIntComponent>()?.value
        }
    }


    private inner class HavingNullsFirstSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = TagComponent::class.having(true)
    ) {

        val iteratedValues = ArrayList<Int>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += if (entity.hasComponent<TagComponent>()) 1 else -1
        }
    }


    private inner class HavingNullsLastSystem : IterableActingSystem(
        only = MustHaveComponent::class,
        orderBy = TagComponent::class.having()
    ) {

        val iteratedValues = ArrayList<Int>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += if (entity.hasComponent<TagComponent>()) 1 else -1
        }
    }


    private inner class CustomOrderSystem : IterableActingSystem(only = MustHaveComponent::class) {

        override val entityOrder = EntityOrder.custom { e1, e2 ->
            e1.id.compareTo(e2.id)
        }

        val iteratedValues = ArrayList<Int>(50_000)

        override fun act(entity: Entity) {
            iteratedValues += entity.id
        }
    }


    private val ascendingNullsFirstImmutableComponentSystem = AscendingNullsFirstImmutableComponentSystem()
    private val ascendingNullsLastImmutableComponentSystem = AscendingNullsLastImmutableComponentSystem()
    private val descendingNullsFirstImmutableComponentSystem = DescendingNullsFirstImmutableComponentSystem()
    private val descendingNullsLastImmutableComponentSystem = DescendingNullsLastImmutableComponentSystem()
    private val ascendingNullsFirstObservableComponentSystem = AscendingNullsFirstObservableComponentSystem()
    private val ascendingNullsLastObservableComponentSystem = AscendingNullsLastObservableComponentSystem()
    private val descendingNullsFirstObservableComponentSystem = DescendingNullsFirstObservableComponentSystem()
    private val descendingNullsLastObservableComponentSystem = DescendingNullsLastObservableComponentSystem()
    private val havingNullsFirstSystem = HavingNullsFirstSystem()
    private val havingNullsLastSystem = HavingNullsLastSystem()
    private val customOrderSystem = CustomOrderSystem()

    private val world = World()

    private val requiredValues = ArrayList<Int?>(50_000)
    private val requiredHaving = ArrayList<Int>(50_000)
    private val entityIds = ArrayList<Int>(50_000)


    @BeforeAll
    fun init() {
        registerSystems()
        addEntities()
        world.act()
    }

    private fun registerSystems() {
        world.registerSystem(ascendingNullsFirstImmutableComponentSystem)
        world.registerSystem(ascendingNullsLastImmutableComponentSystem)
        world.registerSystem(descendingNullsFirstImmutableComponentSystem)
        world.registerSystem(descendingNullsLastImmutableComponentSystem)
        world.registerSystem(ascendingNullsFirstObservableComponentSystem)
        world.registerSystem(ascendingNullsLastObservableComponentSystem)
        world.registerSystem(descendingNullsFirstObservableComponentSystem)
        world.registerSystem(descendingNullsLastObservableComponentSystem)
        world.registerSystem(havingNullsFirstSystem)
        world.registerSystem(havingNullsLastSystem)
        world.registerSystem(customOrderSystem)
    }

    private fun addEntities() {
        val random = Random(717)
        repeat(50_000) {
            val createComponents = random.nextBoolean()
            if (createComponents) {
                val intValue = random.nextInt(0, 1_000_000)
                val entity = world.createEntity(
                    MustHaveComponent,
                    TagComponent,
                    UniqueObservableIntComponent(intValue),
                    ImmutableIntComponent(intValue)
                )
                requiredValues += intValue
                requiredHaving += 1
                entityIds += entity.id
            }
            else {
                val entity = world.createEntity(MustHaveComponent)
                requiredValues += null
                requiredHaving += -1
                entityIds += entity.id
            }
        }
    }


    @Test
    fun ascendingNullsFirstImmutableComponent() {
        val iteratedValues = ascendingNullsFirstImmutableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else -1
            }
            else if (b == null) 1
            else a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun ascendingNullsFirstObservableComponent() {
        val iteratedValues = ascendingNullsFirstObservableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else -1
            }
            else if (b == null) 1
            else a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun ascendingNullsLastImmutableComponent() {
        val iteratedValues = ascendingNullsLastImmutableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else 1
            }
            else if (b == null) -1
            else a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun ascendingNullsLastObservableComponent() {
        val iteratedValues = ascendingNullsLastObservableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else 1
            }
            else if (b == null) -1
            else a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun descendingNullsFirstImmutableComponent() {
        val iteratedValues = descendingNullsFirstImmutableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else -1
            }
            else if (b == null) 1
            else -a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun descendingNullsFirstObservableComponent() {
        val iteratedValues = descendingNullsFirstObservableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else -1
            }
            else if (b == null) 1
            else -a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun descendingNullsLastImmutableComponent() {
        val iteratedValues = descendingNullsLastImmutableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else 1
            }
            else if (b == null) -1
            else -a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun descendingNullsLastObservableComponent() {
        val iteratedValues = descendingNullsLastObservableComponentSystem.iteratedValues
        val requiredValues = requiredValues.sortedWith { a, b ->
            if (a == null) {
                if (b == null) 0
                else 1
            }
            else if (b == null) -1
            else -a.compareTo(b)
        }
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun havingNullsFirst() {
        val iteratedValues = havingNullsFirstSystem.iteratedValues
        val requiredValues = requiredHaving.sorted()
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun havingNullsLast() {
        val iteratedValues = havingNullsLastSystem.iteratedValues
        val requiredValues = requiredHaving.sortedDescending()
        assert(iteratedValues == requiredValues)
    }

    @Test
    fun customOrder() {
        assert(customOrderSystem.iteratedValues == entityIds.sorted())
    }

}