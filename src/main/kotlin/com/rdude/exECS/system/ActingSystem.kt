package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.ActingEvent
import com.rdude.exECS.utils.collections.IterableList
import kotlin.reflect.KClass

abstract class ActingSystem(override val aspect: Aspect = Aspect()) : EventSystem<ActingEvent>(aspect) {

    constructor(
        allOf: MutableList<KClass<out Component>> = mutableListOf(),
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: MutableList<KClass<out Component>> = mutableListOf()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: IterableList<KClass<out Component>> = IterableList(),
        anyOf: IterableList<KClass<out Component>> = IterableList(),
        exclude: IterableList<KClass<out Component>> = IterableList()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        only: KClass<out Component>,
        exclude: KClass<out Component>
    ) : this(
        allOf = IterableList(),
        anyOf = IterableList(only),
        exclude = IterableList(exclude))

    constructor(
        only: KClass<out Component>,
        exclude: IterableList<KClass<out Component>> = IterableList()
    ) : this(
        allOf = IterableList(),
        anyOf = IterableList(only),
        exclude = exclude)

    constructor(only: KClass<out Component>): this(Aspect(only = only))

    final override fun eventFired(entity: Entity, event: ActingEvent) {
        act(entity, event.delta)
    }

    abstract fun act(entity: Entity, delta: Double)

}