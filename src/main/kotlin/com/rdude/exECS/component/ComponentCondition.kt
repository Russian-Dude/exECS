package com.rdude.exECS.component

import com.rdude.exECS.aspect.AspectEntryElement
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.componentTypeId
import kotlin.reflect.KClass

/** Condition that [Component] can meet. [Systems][System] can subscribe to it.*/
sealed class ComponentCondition<T : Component>(@JvmField val componentTypeId: Int) : AspectEntryElement {

    abstract fun test(component: T): Boolean

    companion object {

        operator fun <T : ImmutableComponent> invoke(componentClass: KClass<T>, predicate: T.() -> Boolean) =
            object : ImmutableComponentCondition<T>(componentClass) {
                override fun test(component: T): Boolean = component.predicate()
            }

        operator fun <T : ImmutableComponent> invoke(instance: T) =
            object : ImmutableComponentCondition<T>(instance::class.componentTypeId) {
                override fun test(component: T): Boolean = component == instance
            }

        operator fun <T> invoke(componentClass: KClass<T>, predicate: T.() -> Boolean) where T : ObservableComponent<*>, T : CanBeObservedBySystem =
            object : ObservableComponentCondition<T>(componentClass) {
                override fun test(component: T): Boolean = component.predicate()
            }

    }

}

abstract class ImmutableComponentCondition<T : ImmutableComponent> internal constructor(componentTypeId: Int) :
    ComponentCondition<T>(componentTypeId) {
    constructor(componentCl: KClass<T>) : this(componentCl.componentTypeId)
}

abstract class ObservableComponentCondition<T> internal constructor(componentTypeId: Int) :
    ComponentCondition<T>(componentTypeId) where T : ObservableComponent<*>, T : CanBeObservedBySystem {
    constructor(componentCl: KClass<T>) : this(componentCl.componentTypeId)
}