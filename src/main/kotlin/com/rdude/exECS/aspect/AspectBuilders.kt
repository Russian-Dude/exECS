package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.utils.collections.IterableArray
import kotlin.reflect.KClass

infix fun KClass<out Component>.and(other: KClass<out Component>): IterableArray<KClass<out Component>> {
    val array = IterableArray<KClass<out Component>>()
    array.addAll(this, other)
    return array
}

infix fun IterableArray<KClass<out Component>>.and(other: KClass<out Component>): IterableArray<KClass<out Component>> {
    add(other)
    return this
}