package com.rdude.exECS.component

import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.system.System
import kotlin.reflect.KClass

/** Condition that [ObservableComponent] can meet. [Systems][System] can subscribe to it.*/
interface ComponentCondition<T> : AspectEntryElement where T : ObservableComponent<*>, T : CanBeObservedBySystem {

    val componentClass: KClass<T>

    val predicate: T.() -> Boolean

    fun test(component: T): Boolean = component.predicate()

}


class SimpleComponentCondition<T>(
    override val componentClass: KClass<T>,
    override val predicate: T.() -> Boolean
) : ComponentCondition<T> where T : ObservableComponent<*>, T : CanBeObservedBySystem