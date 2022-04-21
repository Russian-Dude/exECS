package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.ActingEvent
import kotlin.reflect.KClass

abstract class ActingSystem(override val aspect: Aspect = Aspect()) : EventSystem<ActingEvent>(aspect) {

    constructor(
        allOf: List<KClass<out Component>> = listOf(),
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: List<KClass<out Component>> = listOf()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: List<KClass<out Component>> = listOf(),
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: KClass<out Component>
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = listOf(exclude)))

    constructor(
        allOf: KClass<out Component>,
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: KClass<out Component>
    ) : this(allOf = listOf(allOf), anyOf = anyOf, exclude = listOf(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: List<KClass<out Component>> = listOf()
    ) : this(allOf = listOf(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        only: KClass<out Component>,
        exclude: KClass<out Component>
    ) : this(
        allOf = listOf(),
        anyOf = listOf(only),
        exclude = listOf(exclude))

    constructor(
        only: KClass<out Component>,
        exclude: List<KClass<out Component>> = listOf()
    ) : this(
        allOf = listOf(),
        anyOf = listOf(only),
        exclude = exclude)

    constructor(only: KClass<out Component>): this(Aspect(only = only))

    constructor() : this(listOf())

    final override fun eventFired(entity: EntityWrapper, event: ActingEvent) {
        act(entity, event.delta)
    }

    abstract fun act(entity: EntityWrapper, delta: Double)

}