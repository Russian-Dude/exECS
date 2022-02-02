package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.Event
import com.rdude.exECS.utils.collections.IterableArray
import kotlin.reflect.KClass

abstract class EventSystem<T : Event> (override val aspect: Aspect = Aspect()): System() {

    constructor(
        allOf: IterableArray<KClass<out Component>> = IterableArray(true),
        anyOf: IterableArray<KClass<out Component>> = IterableArray(true),
        exclude: IterableArray<KClass<out Component>> = IterableArray(true)
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: MutableList<KClass<out Component>> = mutableListOf(),
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: MutableList<KClass<out Component>> = mutableListOf()
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = exclude))

    constructor(
        allOf: IterableArray<KClass<out Component>> = IterableArray(true),
        anyOf: IterableArray<KClass<out Component>> = IterableArray(true),
        exclude: KClass<out Component>
    ) : this(Aspect(allOf = allOf, anyOf = anyOf, exclude = IterableArray(true, exclude)))

    constructor(
        allOf: KClass<out Component>,
        anyOf: IterableArray<KClass<out Component>> = IterableArray(true),
        exclude: KClass<out Component>
    ) : this(allOf = IterableArray(true, allOf), anyOf = anyOf, exclude = IterableArray(true, exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: IterableArray<KClass<out Component>> = IterableArray(true),
        exclude: IterableArray<KClass<out Component>> = IterableArray(true)
    ) : this(allOf = IterableArray(true, allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        only: KClass<out Component>,
        exclude: KClass<out Component>
    ) : this(
        allOf = IterableArray(true),
        anyOf = IterableArray(true, only),
        exclude = IterableArray(true, exclude))

    constructor(
        only: KClass<out Component>,
        exclude: IterableArray<KClass<out Component>> = IterableArray(true)
    ) : this(
        allOf = IterableArray(true),
        anyOf = IterableArray(true, only),
        exclude = exclude)

    constructor(only: KClass<out Component>): this(Aspect(only = only))


    abstract fun eventFired(entity: EntityWrapper, event: T)

    fun fireEvent(event: T) {
        for (entityID in entityIDs) {
            val entityWrapper = world.entityWrapper
            val entity = world.entityMapper[entityID]
            entityWrapper.entity = entity
            entityWrapper.entityID = entityID
            eventFired(entityWrapper, event)
        }
    }

}