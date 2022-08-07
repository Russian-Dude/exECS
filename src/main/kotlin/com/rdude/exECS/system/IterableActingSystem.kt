package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
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
abstract class IterableActingSystem(aspect: Aspect) : IterableEventSystem<ActingEvent>(aspect) {

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
        exclude = AspectEntry(exclude))

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
        exclude = exclude)

    constructor(
        only: AspectEntryElement,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude)

    constructor(only: KClass<out Component>): this(Aspect(only = only))

    constructor(only: AspectEntryElement): this(Aspect(only = only))


    final override fun eventFired(entity: Entity, event: ActingEvent) {
        act(entity, event.delta)
    }

    /** Implement this method to specify a behaviour when [ActingEvent] is fired.*/
    protected abstract fun act(entity: Entity, delta: Double)

}