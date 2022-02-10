package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.Event
import kotlin.reflect.KClass

abstract class EventSystem<T : Event> (override val aspect: Aspect = Aspect()): System() {

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


    abstract fun eventFired(entity: EntityWrapper, event: T)

    internal fun fireEvent(event: T) {
        for (id in entitiesSubscription.entityIDs) {
            val entityWrapper = world.entityWrapper
            val entityID = EntityID(id)
            val entity = world.entityMapper[entityID]
            entityWrapper.entity = entity
            entityWrapper.entityID = entityID
            eventFired(entityWrapper, event)
        }
    }

}