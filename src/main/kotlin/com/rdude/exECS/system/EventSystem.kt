package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.Event
import kotlin.reflect.KClass

abstract class EventSystem<T : Event> (override val aspect: Aspect = Aspect()): System() {

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
        exclude = listOf(exclude)
    )

    constructor(
        only: KClass<out Component>,
        exclude: List<KClass<out Component>> = listOf()
    ) : this(
        allOf = listOf(),
        anyOf = listOf(only),
        exclude = exclude)

    constructor(only: KClass<out Component>): this(Aspect(only = only))


    abstract fun eventFired(entity: EntityWrapper, event: T)

    internal fun fireEvent(event: T) {
        for (id in entitiesSubscription.entityIDs) {
            val entityWrapper = world.entityWrapper
            entityWrapper.entityID = id
            eventFired(entityWrapper, event)
        }
    }

}