package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.Event
import kotlin.reflect.KClass

abstract class EventSystem<T : Event>(aspect: Aspect = Aspect()) : System(aspect) {

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

    constructor() : this(AspectEntry())

    open fun beforeActing() {}

    open fun afterActing() {}

    abstract fun eventFired(entity: Entity, event: T)

    internal fun fireEvent(event: T) {
        entitiesSubscription.entityIDs.forEach {
            eventFired(Entity(it), event)
        }
    }

}