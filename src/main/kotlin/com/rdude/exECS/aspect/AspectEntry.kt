package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentCondition
import com.rdude.exECS.component.ImmutableComponent
import kotlin.reflect.KClass

/** Part of an [Aspect]. Represents the conditions that the [Component] can meet.*/
data class AspectEntry(
    /** List of possible types of a [Component].*/
    @JvmField internal val types: MutableList<KClass<out Component>> = mutableListOf(),

    /** List of the custom conditions that [Component] can meet.*/
    @JvmField internal val conditions: MutableList<ComponentCondition<*>> = mutableListOf()
) {

    constructor(components: Collection<KClass<out Component>>) : this(types = components.toMutableList())

    constructor(component: KClass<out Component>) : this(types = mutableListOf(component))

    constructor(aspectEntryElement: AspectEntryElement) : this() {
        when (aspectEntryElement) {
            is ComponentCondition<*> -> conditions.add(aspectEntryElement)
            is ImmutableComponent -> conditions.add(ComponentCondition(aspectEntryElement))
            else -> throw NotImplementedError("Can not add AspectEntryElement to the AspectEntry. Adding of ${aspectEntryElement::class} is not implemented")
        }
    }

    /** True if this aspect entry does not describe any conditions.*/
    fun isEmpty(): Boolean = types.isEmpty() && conditions.isEmpty()

    override fun toString(): String {
        val simple = if (types.isEmpty()) null else "types=$types"
        val conditions = if (conditions.isEmpty()) null else "conditions=$conditions"
        return sequenceOf(simple, conditions)
            .filterNotNull()
            .joinToString(prefix = "AspectEntry(", postfix = ")")
    }


}