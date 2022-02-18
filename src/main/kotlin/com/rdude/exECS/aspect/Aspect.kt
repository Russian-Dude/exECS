package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import kotlin.reflect.KClass

class Aspect(
    internal val allOf: List<KClass<out Component>> = listOf(),
    internal val anyOf: List<KClass<out Component>> = listOf(),
    internal val exclude: List<KClass<out Component>> = listOf()
) {

    constructor(): this(listOf(), listOf(), listOf())

    constructor(only: KClass<out Component>): this(anyOf = listOf(only))

    constructor(
        allOf: List<KClass<out Component>> = listOf(),
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: KClass<out Component>
    ) : this(allOf = allOf, anyOf = anyOf, exclude = listOf(exclude))

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aspect) return false
        if (allOf notEqualsWithAnyOrder other.allOf) return false
        if (anyOf notEqualsWithAnyOrder other.anyOf) return false
        if (exclude notEqualsWithAnyOrder other.exclude) return false
        return true
    }

    override fun hashCode(): Int {
        var result = allOf.hashCode()
        result = 31 * result + anyOf.hashCode()
        result = 31 * result + exclude.hashCode()
        return result
    }

    private infix fun List<*>.equalsWithAnyOrder(other: List<*>): Boolean {
        if (this === other) return true
        if (size != other.size) return false
        for (i in 0..this.size - 1) {
            var has = false
            for (j in 0..this.size - 1) {
                if (this[i] == other[j]) {
                    has = true
                    continue
                }
            }
            if (!has) return false
        }
        return true
    }

    private infix fun List<*>.notEqualsWithAnyOrder(other: List<*>) = !equalsWithAnyOrder(other)


}