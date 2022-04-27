package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.State
import kotlin.reflect.KClass

class Aspect(
    internal val allOf: AspectEntry = AspectEntry(),
    internal val anyOf: AspectEntry = AspectEntry(),
    internal val exclude: AspectEntry = AspectEntry()
) {

    constructor(): this(AspectEntry(), AspectEntry(), AspectEntry())

    constructor(
        allOf: List<KClass<out Component>> = listOf(),
        anyOf: List<KClass<out Component>> = listOf(),
        exclude: List<KClass<out Component>> = listOf()
    ) : this(allOf = AspectEntry(allOf), anyOf = AspectEntry(anyOf), exclude = AspectEntry(exclude))

    constructor(only: KClass<out Component>): this(anyOf = listOf(only))

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: AspectEntry = AspectEntry(),
        anyOf: AspectEntry = AspectEntry(),
        exclude: State
    ) : this(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: State,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: State
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: State,
        anyOf: AspectEntry = AspectEntry(),
        exclude: State
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry()
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        allOf: State,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry()
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        only: KClass<out Component>,
        exclude: KClass<out Component>
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: State,
        exclude: KClass<out Component>
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: KClass<out Component>,
        exclude: State
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: State,
        exclude: State
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: KClass<out Component>,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude)

    constructor(
        only: State,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aspect) return false
        if (allOf.simpleComponents notEqualsWithAnyOrder other.allOf.simpleComponents) return false
        if (allOf.stateComponents notEqualsWithAnyOrder other.allOf.stateComponents) return false
        if (anyOf.simpleComponents notEqualsWithAnyOrder other.anyOf.simpleComponents) return false
        if (anyOf.stateComponents notEqualsWithAnyOrder other.anyOf.stateComponents) return false
        if (exclude.simpleComponents notEqualsWithAnyOrder other.exclude.simpleComponents) return false
        if (exclude.stateComponents notEqualsWithAnyOrder other.exclude.stateComponents) return false
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