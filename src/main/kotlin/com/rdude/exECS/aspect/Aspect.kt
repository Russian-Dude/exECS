package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.utils.collections.IterableList
import kotlin.reflect.KClass

class Aspect(
    val allOf: IterableList<KClass<out Component>> = IterableList(),
    val anyOf: IterableList<KClass<out Component>> = IterableList(),
    val exclude: IterableList<KClass<out Component>> = IterableList()
) {

    constructor(): this(IterableList(), IterableList(), IterableList())

    constructor(only: KClass<out Component>): this() {
        anyOf.add(only)
    }

    constructor(
        allOf: List<KClass<out Component>> = mutableListOf(),
        anyOf: List<KClass<out Component>> = mutableListOf(),
        exclude: List<KClass<out Component>> = mutableListOf()
    ) : this(IterableList(), IterableList(), IterableList()) {
        allOf.forEach { this.allOf.add(it) }
        anyOf.forEach { this.anyOf.add(it) }
        exclude.forEach { this.exclude.add(it) }
    }

    constructor(
        allOf: IterableList<KClass<out Component>> = IterableList(),
        anyOf: IterableList<KClass<out Component>> = IterableList(),
        exclude: KClass<out Component>
    ) : this(allOf = allOf, anyOf = anyOf, exclude = IterableList(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: IterableList<KClass<out Component>> = IterableList(),
        exclude: KClass<out Component>
    ) : this(allOf = IterableList(allOf), anyOf = anyOf, exclude = IterableList(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: IterableList<KClass<out Component>> = IterableList(),
        exclude: IterableList<KClass<out Component>> = IterableList()
    ) : this(allOf = IterableList(allOf), anyOf = anyOf, exclude = exclude)

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

}