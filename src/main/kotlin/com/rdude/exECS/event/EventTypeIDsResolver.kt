package com.rdude.exECS.event

import com.rdude.exECS.utils.hash.HashFunctionGenerator
import com.rdude.exECS.utils.reflection.ReflectionUtils
import kotlin.reflect.KClass

internal object EventTypeIDsResolver {

    private val divider: Int
    private val modulo: Int
    private val hasModulo: Boolean

    internal val size: Int

    init {
        val allComponentClasses = ReflectionUtils.getNotAbstractSubClassesFromAllPackages(Event::class)
        val functionArguments = HashFunctionGenerator.generateFunctionArguments(allComponentClasses)
        size = functionArguments.size
        divider = functionArguments.divider
        modulo = functionArguments.modulo
        hasModulo = functionArguments.hasModulo
    }

    fun idFor(eventClass: KClass<out Event>): EventTypeID = EventTypeID(
        if (hasModulo) (eventClass.hashCode() / divider) % modulo
        else eventClass.hashCode() / divider
    )

}