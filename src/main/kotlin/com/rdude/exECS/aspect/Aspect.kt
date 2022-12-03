package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentCondition
import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

/** Describes what the [Component] should be in order for it to be interested in.*/
class Aspect(
    @JvmField internal val allOf: AspectEntry = AspectEntry(),
    @JvmField internal val anyOf: AspectEntry = AspectEntry(),
    @JvmField internal val exclude: AspectEntry = AspectEntry()
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
        exclude: AspectEntryElement
    ) : this(allOf = allOf, anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: KClass<out Component>
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: AspectEntryElement,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntryElement
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = AspectEntry(exclude))

    constructor(
        allOf: KClass<out Component>,
        anyOf: AspectEntry = AspectEntry(),
        exclude: AspectEntry = AspectEntry()
    ) : this(allOf = AspectEntry(allOf), anyOf = anyOf, exclude = exclude)

    constructor(
        allOf: AspectEntryElement,
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
        only: AspectEntryElement,
        exclude: KClass<out Component>
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: KClass<out Component>,
        exclude: AspectEntryElement
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = AspectEntry(exclude)
    )

    constructor(
        only: AspectEntryElement,
        exclude: AspectEntryElement
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
        only: AspectEntryElement,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude)

    constructor(
        only: ComponentCondition<*>,
        exclude: AspectEntry = AspectEntry()
    ) : this(
        allOf = AspectEntry(),
        anyOf = AspectEntry(only),
        exclude = exclude)

    init {
        ExEcs.aspectCorrectnessChecker.checkAndThrowIfNotCorrect(this)
    }

    fun isEmpty() = allOf.isEmpty() && anyOf.isEmpty() && exclude.isEmpty()

    /** @return all unique Component type ids from [allOf], [anyOf] and [exclude].*/
    internal fun getComponentTypeIds(): Sequence<Int> =
        (allOf.getSequenceOfAllComponentTypeIds() + anyOf.getSequenceOfAllComponentTypeIds() + exclude.getSequenceOfAllComponentTypeIds())
            .distinct()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Aspect) return false
        if (allOf.types notEqualsWithAnyOrder other.allOf.types) return false
        if (allOf.conditions notEqualsWithAnyOrder other.allOf.conditions) return false
        if (anyOf.types notEqualsWithAnyOrder other.anyOf.types) return false
        if (anyOf.conditions notEqualsWithAnyOrder other.anyOf.conditions) return false
        if (exclude.types notEqualsWithAnyOrder other.exclude.types) return false
        if (exclude.conditions notEqualsWithAnyOrder other.exclude.conditions) return false
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
    override fun toString(): String {
        val allOfString = if (allOf.isEmpty()) null else "allOf=$allOf"
        val anyOfString = if (anyOf.isEmpty()) null else "anyOf=$anyOf"
        val excludeString = if (exclude.isEmpty()) null else "exclude=$exclude"
        return sequenceOf(allOfString, anyOfString, excludeString)
            .filterNotNull()
            .joinToString(prefix = "Aspect(", postfix = ")")
    }


}