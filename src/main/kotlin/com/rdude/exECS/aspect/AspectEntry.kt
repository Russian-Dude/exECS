package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentCondition
import com.rdude.exECS.component.ImmutableComponent
import kotlin.reflect.KClass

/** Part of an [Aspect]. Represents the conditions that the [Component] can meet.*/
data class AspectEntry(
    /** List of possible types of a [Component].*/
    @JvmField internal val types: MutableList<KClass<out Component>> = mutableListOf(),

    /** List of the [ImmutableComponent]s that [Component] can be equal to (if it is also a [ImmutableComponent]).*/
    @JvmField internal val immutableComponents: MutableList<ImmutableComponent> = mutableListOf(),

    /** List of the custom conditions that [Component] can meet.*/
    @JvmField internal val conditions: MutableList<ComponentCondition<*>> = mutableListOf()
) {

    constructor() : this(mutableListOf(), mutableListOf(), mutableListOf())

    constructor(components: List<KClass<out Component>>) : this() {
        types.addAll(components)
    }

    constructor(component: KClass<out Component>) : this() {
        types.add(component)
    }

    constructor(aspectEntryElement: AspectEntryElement) : this() {
        when (aspectEntryElement) {
            is ImmutableComponent -> immutableComponents.add(aspectEntryElement)
            is ComponentCondition<*> -> conditions.add(aspectEntryElement)
            else -> throw NotImplementedError("Can not add AspectEntryElement to the AspectEntry. Adding of ${aspectEntryElement::class} is not implemented")
        }
    }

    /** True if this aspect entry does not describe any conditions.*/
    fun isEmpty(): Boolean = types.isEmpty() && immutableComponents.isEmpty() && conditions.isEmpty()
    override fun toString(): String {
        val simple = if (types.isEmpty()) null else "types=$types"
        val state = if (immutableComponents.isEmpty()) null else "states=$immutableComponents"
        val conditions = if (conditions.isEmpty()) null else "conditions=$conditions"
        return sequenceOf(simple, state, conditions)
            .filterNotNull()
            .joinToString(prefix = "AspectEntry(", postfix = ")")
    }


}