package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.State
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.ActingEvent
import kotlin.reflect.KClass

abstract class ActingSystem(aspect: Aspect = Aspect()) : EventSystem<ActingEvent>(aspect) {

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
        exclude: State
    ) : this(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: State,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: State
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: State,
        anyOf: AspectEntry = AspectEntry(),
        exclude: State
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry()
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        allOf: State,
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
        only: State,
        exclude: KClass<out Component>
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: KClass<out Component>,
        exclude: State
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: State,
        exclude: State
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
        only: State,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude)

    constructor(only: KClass<out Component>): this(Aspect(only = only))

    constructor(only: State): this(Aspect(only = only))

    constructor() : this(AspectEntry())

    final override fun eventFired(entity: EntityWrapper, event: ActingEvent) {
        act(entity, event.delta)
    }

    abstract fun act(entity: EntityWrapper, delta: Double)

}