package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import kotlin.reflect.KClass

class Aspect(
    internal val allOf: MutableList<KClass<out Component>> = mutableListOf(),
    internal val anyOf: MutableList<KClass<out Component>> = mutableListOf(),
    internal val exclude: MutableList<KClass<out Component>> = mutableListOf()
) {

    constructor(): this(mutableListOf(), mutableListOf(), mutableListOf())

    constructor(only: KClass<out Component>): this() {
        anyOf.add(only)
    }

    constructor(
        allOf: MutableList<KClass<out Component>> = mutableListOf(),
        anyOf: MutableList<KClass<out Component>> = mutableListOf(),
        exclude: KClass<out Component>
    ) : this(allOf = allOf, anyOf = anyOf, exclude = mutableListOf(exclude))

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

    private infix fun MutableList<*>.equalsWithAnyOrder(other: MutableList<*>): Boolean {
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

    private infix fun MutableList<*>.notEqualsWithAnyOrder(other: MutableList<*>) = !equalsWithAnyOrder(other)


}