package com.rdude.exECS.component

import com.rdude.exECS.utils.hash.HashFunctionGenerator
import com.rdude.exECS.utils.reflection.ReflectionUtils
import kotlin.reflect.KClass

internal object ComponentTypeIDsResolver {

    private val divider: Int
    private val modulo: Int
    private val hasModulo: Boolean

    internal val size: Int

    init {
        val allComponentClasses = ReflectionUtils.getNotAbstractSubClassesFromAllPackages(Component::class)
        val functionArguments = HashFunctionGenerator.generateFunctionArguments(allComponentClasses)
        size = functionArguments.size
        divider = functionArguments.divider
        modulo = functionArguments.modulo
        hasModulo = functionArguments.hasModulo
    }

    fun idFor(componentClass: KClass<out Component>): ComponentTypeID = ComponentTypeID(
        if (hasModulo) (componentClass.hashCode() / divider) % modulo
        else componentClass.hashCode() / divider
    )

}