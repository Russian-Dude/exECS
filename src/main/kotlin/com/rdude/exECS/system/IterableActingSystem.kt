package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityOrder
import com.rdude.exECS.event.ActingEvent
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

/** System that is being triggered every [World.act] method execution and iterates over the [Entities][Entity] it is subscribed to.
 *
 * Check [IterableEventSystem] for information about subscribing to Entities.
 * @see System
 * @see EventSystem
 * @see IterableEventSystem
 * @see ActingSystem*/
abstract class IterableActingSystem(aspect: Aspect, orderBy: EntityOrder.Definition) : IterableEventSystem<ActingEvent>(aspect, orderBy) {

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
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude)), orderBy = orderBy)

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

    constructor(only: KClass<out Component>, orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified):
            this(Aspect(only = only), orderBy)

    constructor(only: AspectEntryElement, orderBy: EntityOrder.Definition = EntityOrder.Definition.NotSpecified):
            this(Aspect(only = only), orderBy)


    final override fun eventFired(entity: Entity, event: ActingEvent) {
        act(entity)
    }

    /** Implement this method to specify a behaviour when [ActingEvent] is fired.*/
    protected abstract fun act(entity: Entity)

}