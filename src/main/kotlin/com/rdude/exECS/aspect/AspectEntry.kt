package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.State
import kotlin.reflect.KClass

data class AspectEntry(
    internal val simpleComponents: MutableList<KClass<out Component>> = mutableListOf(),

    internal val stateComponents: MutableList<State> = mutableListOf()
) {

    constructor() : this(mutableListOf(), mutableListOf())

    constructor(components: List<KClass<out Component>>) : this() {
        simpleComponents.addAll(components)
    }

    constructor(component: KClass<out Component>) : this() {
        simpleComponents.add(component)
    }

    constructor(component: State) : this() {
        stateComponents.add(component)
    }

    fun isEmpty(): Boolean = simpleComponents.isEmpty() && stateComponents.isEmpty()

}