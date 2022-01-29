package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.Event
import com.rdude.exECS.utils.collections.IterableList
import kotlin.reflect.KClass

abstract class EventSystem<T : Event> (override val aspect: Aspect = Aspect()): System() {

    constructor(
        allOf: IterableList<KClass<out Component>> = IterableList(),
        anyOf: IterableList<KClass<out Component>> = IterableList(),
        exclude: IterableList<KClass<out Component>> = IterableList()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: MutableList<KClass<out Component>> = mutableListOf(),
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: MutableList<KClass<out Component>> = mutableListOf()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: IterableList<KClass<out Component>> = IterableList(),
        anyOf: IterableList<KClass<out Component>> = IterableList(),
        exclude: KClass<out Component>? = null
    ) : this(
        allOf = allOf,
        anyOf = anyOf,
        exclude = exclude?.let { IterableList(it) } ?: IterableList())

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


    abstract fun eventFired(entity: Entity, event: T)

    fun fireEvent(event: T) {
        for (entity in entities) {
            eventFired(entity, event)
        }
    }

}