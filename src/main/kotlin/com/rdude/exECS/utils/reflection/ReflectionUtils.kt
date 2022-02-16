package com.rdude.exECS.utils.reflection

import org.reflections.Reflections
import kotlin.reflect.KClass

internal object ReflectionUtils {

    internal val eventSystemGenericQualifier = EventSystemGenericQualifier()

    internal fun <T : Any> getNotAbstractSubClassesFromAllPackages(kClass: KClass<T>) : Set<KClass<out T>> {
        return Package.getPackages()
            .asSequence()
            .map { it.name.substringBefore('.') }
            .distinct()
            .map { Reflections(it) }
            .flatMap { it.getSubTypesOf(kClass.java) }
            .map { it.kotlin }
            .filterNot { it.isAbstract }
            .toSet()
    }

}