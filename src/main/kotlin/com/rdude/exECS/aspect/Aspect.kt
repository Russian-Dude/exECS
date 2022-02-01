package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.utils.collections.IterableArray
import kotlin.reflect.KClass

class Aspect(
    val allOf: IterableArray<KClass<out Component>> = IterableArray(true),
    val anyOf: IterableArray<KClass<out Component>> = IterableArray(true),
    val exclude: IterableArray<KClass<out Component>> = IterableArray(true)
) {

    constructor(): this(IterableArray(true), IterableArray(true), IterableArray(true))

    constructor(only: KClass<out Component>): this() {
        anyOf.add(only)
    }

    constructor(
        allOf: List<KClass<out Component>> = mutableListOf(),
        anyOf: List<KClass<out Component>> = mutableListOf(),
        exclude: List<KClass<out Component>> = mutableListOf()
    ) : this(IterableArray(), IterableArray(), IterableArray()) {
        allOf.forEach { this.allOf.add(it) }
        anyOf.forEach { this.anyOf.add(it) }
        exclude.forEach { this.exclude.add(it) }
    }

    constructor(
        allOf: IterableArray<KClass<out Component>> = IterableArray(true),
        anyOf: IterableArray<KClass<out Component>> = IterableArray(true),
        exclude: KClass<out Component>
    ) : this(allOf = allOf, anyOf = anyOf, exclude = IterableArray(true, exclude))

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

}