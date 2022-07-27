package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.*
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.Event
import kotlin.reflect.KClass

abstract class IterableEventSystem<T : Event>(val aspect: Aspect) : EventSystem<T>() {

    @JvmField internal var entitiesSubscription: EntitiesSubscription? = null

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: List<KClass<out Component>> = listOf(),
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: List<KClass<out Component>> = listOf()
    ) : this(allOf = AspectEntry(allOf), anyOf = AspectEntry(anyOf), exclude = AspectEntry(exclude))

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude)))

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement
    ) : this(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry()
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry()
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        only: KClass<out Component>,
        exclude: KClass<out Component>
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: AspectEntryElement,
        exclude: KClass<out Component>
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: KClass<out Component>,
        exclude: AspectEntryElement
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: AspectEntryElement,
        exclude: AspectEntryElement
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: KClass<out Component>,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude
    )

    constructor(
        only: AspectEntryElement,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude
    )

    constructor(only: KClass<out Component>) : this(Aspect(only = only))

    constructor(only: AspectEntryElement) : this(Aspect(only = only))

    protected open fun beforeIteration() {}

    protected open fun afterIteration() {}

    protected abstract fun eventFired(entity: Entity, event: T)

    final override fun eventFired(event: T) {
        // Exception should never occur
        val subscription = entitiesSubscription ?: throw NullPointerException("Entities subscription property of System of type ${this::class} is null")
        beforeIteration()
        subscription.entityIDs.forEach {
            eventFired(Entity(it), event)
        }
        afterIteration()
    }


    // functions in this companion will be moved to extensions after updating Kotlin to the version that supports @Scope.
    protected companion object {

        @JvmStatic
        protected inline operator fun <reified T> KClass<T>.invoke(noinline condition: T.() -> Boolean): ComponentCondition<T> where T : ObservableComponent<*>, T : CanBeObservedBySystem =
            SimpleComponentCondition(T::class, condition)

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.types.add(this)
            entry.types.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ImmutableComponent.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.immutableComponents.add(this)
            entry.types.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: ImmutableComponent): AspectEntry {
            val entry = AspectEntry()
            entry.types.add(this)
            entry.immutableComponents.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ImmutableComponent.and(other: ImmutableComponent): AspectEntry {
            val entry = AspectEntry()
            entry.immutableComponents.add(this)
            entry.immutableComponents.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: KClass<out Component>): AspectEntry {
            types.add(other)
            return this
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: ImmutableComponent): AspectEntry {
            immutableComponents.add(other)
            return this
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: ComponentCondition<*>): AspectEntry {
            conditions.add(other)
            return this
        }

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: ComponentCondition<*>): AspectEntry {
            val entry = AspectEntry()
            entry.types.add(this)
            entry.conditions.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ImmutableComponent.and(other: ComponentCondition<*>): AspectEntry {
            val entry = AspectEntry()
            entry.immutableComponents.add(this)
            entry.conditions.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: ComponentCondition<*>): AspectEntry {
            val entry = AspectEntry()
            entry.conditions.add(this)
            entry.conditions.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: ImmutableComponent): AspectEntry {
            val entry = AspectEntry()
            entry.conditions.add(this)
            entry.immutableComponents.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.conditions.add(this)
            entry.types.add(other)
            return entry
        }
    }

}