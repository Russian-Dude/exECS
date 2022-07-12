package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentCondition
import com.rdude.exECS.component.State
import kotlin.reflect.KClass

/** Part of an [Aspect]. Represents the conditions that the [Component] can meet.*/
data class AspectEntry(
    /** List of possible types of a [Component].*/
    @JvmField internal val types: MutableList<KClass<out Component>> = mutableListOf(),

    /** List of the [State]s that [Component] can be equal to (if it is also a [State]).*/
    @JvmField internal val states: MutableList<State> = mutableListOf(),

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
            is State -> states.add(aspectEntryElement)
            is ComponentCondition<*> -> conditions.add(aspectEntryElement)
            else -> throw NotImplementedError("Can not add AspectEntryElement to the AspectEntry. Adding of ${aspectEntryElement::class} is not implemented")
        }
    }

    /** True if this aspect entry does not describe any conditions.*/
    fun isEmpty(): Boolean = types.isEmpty() && states.isEmpty() && conditions.isEmpty()
    override fun toString(): String {
        val simple = if (types.isEmpty()) null else "types=$types"
        val state = if (states.isEmpty()) null else "states=$states"
        val conditions = if (conditions.isEmpty()) null else "conditions=$conditions"
        return sequenceOf(simple, state, conditions)
            .filterNotNull()
            .joinToString(prefix = "AspectEntry(", postfix = ")")
    }


}