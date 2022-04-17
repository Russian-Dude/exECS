package com.rdude.exECS.utils.reflection

import org.reflections.Reflections
import kotlin.reflect.KClass

internal class ReflectionUtils {

    private val notAbstractSubClassesCache = mutableMapOf<KClass<*>, Set<KClass<*>>>()

    internal fun <T : Any> getNotAbstractSubClassesFromAllPackages(kClass: KClass<T>) : Set<KClass<out T>> {
        val cached = notAbstractSubClassesCache[kClass]
        if (cached != null) return cached as Set<KClass<out T>>
        val result = Package.getPackages()
            .asSequence()
            .map { it.name.substringBefore('.') }
            .distinct()
            .map { Reflections(it) }
            .flatMap { it.getSubTypesOf(kClass.java) }
            .map { it.kotlin }
            .filterNot { it.isAbstract }
            .toSet()
        notAbstractSubClassesCache[kClass] = result
        return result
    }

}