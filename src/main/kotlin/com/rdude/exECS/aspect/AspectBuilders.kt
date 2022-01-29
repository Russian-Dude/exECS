package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.utils.collections.IterableList
import kotlin.reflect.KClass

operator fun KClass<out Component>.plus(component: KClass<out Component>) = this and component

operator fun IterableList<KClass<out Component>>.plus(component: KClass<out Component>) = this and component

infix fun KClass<out Component>.and(other: KClass<out Component>): IterableList<KClass<out Component>> {
    val list = IterableList<KClass<out Component>>()
    list.addAll(this, other)
    return list
}

infix fun IterableList<KClass<out Component>>.and(other: KClass<out Component>): IterableList<KClass<out Component>> {
    add(other)
    return this
}