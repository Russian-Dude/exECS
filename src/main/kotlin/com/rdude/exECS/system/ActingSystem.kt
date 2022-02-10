package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.ActingEvent
import kotlin.reflect.KClass

abstract class ActingSystem(override val aspect: Aspect = Aspect()) : EventSystem<ActingEvent>(aspect) {

    constructor(
        allOf: MutableList<KClass<out Component>> = mutableListOf(),
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: MutableList<KClass<out Component>> = mutableListOf()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: MutableList<KClass<out Component>> = mutableListOf(),
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: KClass<out Component>
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = mutableListOf(exclude)))

    constructor(
        allOf: KClass<out Component>,
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: KClass<out Component>
    ) : this(allOf = mutableListOf(allOf), anyOf = anyOf, exclude = mutableListOf(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: MutableList<KClass<out Component>> = mutableListOf()
    ) : this(allOf = mutableListOf(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        only: KClass<out Component>,
        exclude: KClass<out Component>
    ) : this(
        allOf = mutableListOf(),
        anyOf = mutableListOf(only),
        exclude = mutableListOf(exclude))

    constructor(
        only: KClass<out Component>,
        exclude: MutableList<KClass<out Component>> = mutableListOf()
    ) : this(
        allOf = mutableListOf(),
        anyOf = mutableListOf(only),
        exclude = exclude)

    constructor(only: KClass<out Component>): this(Aspect(only = only))

    final override fun eventFired(entity: EntityWrapper, event: ActingEvent) {
        act(entity, event.delta)
    }

    abstract fun act(entity: EntityWrapper, delta: Double)

}