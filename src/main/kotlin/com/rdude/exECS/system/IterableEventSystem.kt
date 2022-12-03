package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.*
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityOrder
import com.rdude.exECS.event.Event
import com.rdude.exECS.system.IterableEventSystem.Companion.and
import com.rdude.exECS.system.IterableEventSystem.Companion.having
import kotlin.reflect.KClass

/** System that Subscribes to [Events][Event] and [Entities][Entity]. Every time an Event of type [T] (or its subtype)
 * is fired, [eventFired] method is called for every Entity this System is subscribed to.
 * To add the behaviour before and after iteration over the Entities,
 * [beforeIteration] and [afterIteration] methods can be overridden.
 *
 * To specify an Events, this System will be subscribed to, pass Event type as a generic parameter (see [EventSystem]).
 *
 * To specify to which Entities this System will subscribe to and track, an [Aspect] must be passed.
 * The preferred way to do this is to pass the conditions an Entity must meet, separated by the [and] infix function
 * to the constructor's named parameters:
 *
 * * **allOf** - an Entity must satisfy all of these conditions
 * * **anyOf** - an Entity must satisfy any of these conditions
 * * **exclude** - an Entity must satisfy none of these conditions
 * * **only** - same as anyOf but accepts only one condition
 *
 * Conditions can be represented by:
 * * ``MyComponent::class`` - Entities that has a [Component] of the specified type
 * * ``myComponentInstance`` - Entities that has a [ImmutableComponent] that is equals to the specified Component instance
 * * ``MyComponent::class { value > 15 }`` - Entities that has an [ImmutableComponent] or [ObservableComponent] of the specified type
 * which is satisfied the given predicate. Observable Component must also implement either [UniqueComponent] or [RichComponent]
 *
 *
 * By default, the order of the Entities is not constant and may change over time.
 * To specify an order of iteration, pass [EntityOrder.Definition] to the **orderBy** named constructor argument.
 * Order definition can be represented by:
 * * ``MyComponent::class.``[ascending]``()``
 * * ``MyComponent::class.``[descending]``()``
 * * ``MyComponent::class.``[having]``()``
 *
 * Alternatively, [entityOrder] property can be overridden if a more complex comparison is needed.
 * ```
 * ```
 * ***Example:***
 * ```
 * class MyComponent : Component
 *
 * enum class Color : ImmutableComponent { RED, GREEN, BLUE }
 *
 * class ScoreComponent : ObservableIntComponent(), UniqueComponent
 *
 * class MySystem : IterableEventSystem<MyEvent>(
 *      allOf = MyComponent::class and Color.RED,
 *      exclude = ScoreComponent::class { value > 100 },
 *      orderBy = ScoreComponent::class.ascending()
 *      )
 * ```
 *
 * @see System
 * @see EventSystem
 * @see ActingSystem
 * @see IterableActingSystem
 *  */
abstract class IterableEventSystem<T : Event>(val aspect: Aspect, orderBy: EntityOrder.Definition) : EventSystem<T>() {

    open val entityOrder: EntityOrder = generateEntityOrder(orderBy)

    @JvmField internal var entitiesSubscription: EntitiesSubscription? = null

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry(),
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude), orderBy)

    constructor(
        allOf: List<KClass<out Component>> = listOf(),
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: List<KClass<out Component>> = listOf(),
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = AspectEntry(allOf), anyOf = AspectEntry(anyOf), exclude = AspectEntry(exclude), orderBy = orderBy)

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude)), orderBy)

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude), orderBy = orderBy)

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude), orderBy = orderBy)

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude), orderBy = orderBy)

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude), orderBy = orderBy)

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude), orderBy = orderBy)

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry(),
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude, orderBy = orderBy)

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry(),
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude, orderBy = orderBy)

    constructor(
        only: KClass<out Component>,
        exclude: KClass<out Component>,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude),
        orderBy = orderBy
    )

    constructor(
        only: AspectEntryElement,
        exclude: KClass<out Component>,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude),
        orderBy = orderBy
    )

    constructor(
        only: KClass<out Component>,
        exclude: AspectEntryElement,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude),
        orderBy = orderBy
    )

    constructor(
        only: AspectEntryElement,
        exclude: AspectEntryElement,
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude),
        orderBy = orderBy
    )

    constructor(
        only: KClass<out Component>,
        exclude: AspectEntry = AspectEntry(),
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude,
        orderBy = orderBy
    )

    constructor(
        only: AspectEntryElement,
        exclude: AspectEntry = AspectEntry(),
        orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude,
        orderBy = orderBy
    )

    constructor(only: KClass<out Component>, orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified) :
            this(Aspect(only = only), orderBy)

    constructor(only: AspectEntryElement, orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified) :
            this(Aspect(only = only), orderBy)

    /** This method is called before this System starts iterating over the [Entities][Entity] it is subscribed to.
     * @return Boolean value representing whether an iteration should be performed.
     * If false, [eventFired] and [afterIteration] methods will not be executed.*/
    protected open fun beforeIteration(): Boolean = true

    /** This method is called after this System completes the iteration over the [Entities][Entity] it is subscribed to.*/
    protected open fun afterIteration() {}

    /** Implement this method to specify a behaviour when an Event of type [T] (or its subtype) is fired.*/
    protected abstract fun eventFired(entity: Entity, event: T)

    final override fun eventFired(event: T) {
        // Exception should never occur
        val subscription = entitiesSubscription ?: throw NullPointerException("Entities subscription property of System of type ${this::class} is null")
        if (beforeIteration()) {
            subscription.entities.forEach {
                eventFired(Entity(it), event)
            }
            afterIteration()
        }
    }

    // functions in this companion will be moved to extensions after updating Kotlin to the version that supports both context and compiler plugins.
    protected companion object {

        @JvmStatic
        @JvmName("createObservableComponentCondition")
        protected inline operator fun <reified T> KClass<T>.invoke(noinline condition: T.() -> Boolean): ComponentCondition<T> where T : ObservableComponent<*>, T : CanBeObservedBySystem =
            ComponentCondition(T::class, condition)

        @JvmStatic
        @JvmName("createImmutableComponentCondition")
        protected inline operator fun <reified T : ImmutableComponent> KClass<T>.invoke(noinline condition: T.() -> Boolean): ComponentCondition<T> =
            ComponentCondition(T::class, condition)

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: KClass<out Component>): AspectEntry =
            AspectEntry(mutableListOf(this, other))

        @JvmStatic
        protected infix fun ImmutableComponent.and(other: KClass<out Component>): AspectEntry =
            AspectEntry(mutableListOf(other), mutableListOf(ComponentCondition(this)))

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: ImmutableComponent): AspectEntry =
            AspectEntry(mutableListOf(this), mutableListOf(ComponentCondition(other)))

        @JvmStatic
        protected infix fun ImmutableComponent.and(other: ImmutableComponent): AspectEntry =
            AspectEntry(conditions = mutableListOf(ComponentCondition(this), ComponentCondition(other)))

        @JvmStatic
        protected infix fun AspectEntry.and(other: KClass<out Component>): AspectEntry =
            apply { types.add(other) }

        @JvmStatic
        protected infix fun AspectEntry.and(other: ImmutableComponent): AspectEntry =
            apply { conditions.add(ComponentCondition(other)) }

        @JvmStatic
        protected infix fun AspectEntry.and(other: ComponentCondition<*>): AspectEntry =
            apply { conditions.add(other) }

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: ComponentCondition<*>): AspectEntry =
            AspectEntry(mutableListOf(this), mutableListOf(other))

        @JvmStatic
        protected infix fun ImmutableComponent.and(other: ComponentCondition<*>): AspectEntry =
            AspectEntry(conditions = mutableListOf(ComponentCondition(this), other))

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: ComponentCondition<*>): AspectEntry =
            AspectEntry(conditions = mutableListOf(this, other))

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: ImmutableComponent): AspectEntry =
            AspectEntry(conditions = mutableListOf(this, ComponentCondition(other)))

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: KClass<out Component>): AspectEntry =
            AspectEntry(mutableListOf(other), mutableListOf(this))

        @JvmStatic
        @JvmName("ascendingByObservable")
        protected fun <T> KClass<T>.ascending(nullsFirst: Boolean = false): EntityOrder.Definition.By<T> where T : ObservableComponent<*>, T : CanBeObservedBySystem, T : Comparable<T> =
            EntityOrder.Definition.By(cl = this, ascending = true, nullsFirst)

        @JvmStatic
        @JvmName("ascendingByImmutable")
        protected fun <T> KClass<T>.ascending(nullsFirst: Boolean = false): EntityOrder.Definition.By<T> where T : ImmutableComponent, T : Comparable<T> =
            EntityOrder.Definition.By(cl = this, ascending = true, nullsFirst)

        @JvmStatic
        @JvmName("descendingByObservable")
        protected fun <T> KClass<T>.descending(nullsFirst: Boolean = false): EntityOrder.Definition.By<T> where T : ObservableComponent<*>, T : CanBeObservedBySystem, T : Comparable<T> =
            EntityOrder.Definition.By(cl = this, ascending = false, nullsFirst)

        @JvmStatic
        @JvmName("descendingByImmutable")
        protected fun <T> KClass<T>.descending(nullsFirst: Boolean = false): EntityOrder.Definition.By<T> where T : ImmutableComponent, T : Comparable<T> =
            EntityOrder.Definition.By(cl = this, ascending = false, nullsFirst)

        /** Order [Entities][Entity] based on presence of this [Component] type.*/
        @JvmStatic
        protected fun KClass<out Component>.having(nullsFirst: Boolean = false): EntityOrder.Definition.Having =
            EntityOrder.Definition.Having(this, nullsFirst)
    }

}