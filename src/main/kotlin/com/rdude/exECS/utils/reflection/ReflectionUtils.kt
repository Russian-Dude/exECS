package com.rdude.exECS.utils.reflection

import com.rdude.exECS.component.Component
import org.reflections.Reflections
import kotlin.reflect.KClass

internal object ReflectionUtils {

    internal val eventSystemGenericQualifier = EventSystemGenericQualifier()

    internal fun getAllComponentClasses() : Set<KClass<out Component>> {
        return Package.getPackages()
            .asSequence()
            .map { it.name.substringBefore('.') }
            .distinct()
            .map { Reflections(it) }
            .flatMap { it.getSubTypesOf(Component::class.java) }
            .map { it.kotlin }
            .filterNot { it.isAbstract }
            .toSet()
    }

}