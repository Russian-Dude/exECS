package com.rdude.exECS.system

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

internal class SystemTypeIdResolver {

    private val classToIdMap: Map<KClass<out System>, Int>
    internal val size: Int

    init {
        val classToIdMap: MutableMap<KClass<out System>, Int> = HashMap()

        val systemClasses = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(System::class)

        size = systemClasses.size

        classToIdMap += systemClasses
            .mapIndexed { index, kClass -> Pair(kClass, index) }
            .toMap()

        this.classToIdMap = classToIdMap
    }


    fun idFor(systemClass: KClass<out System>): Int =
        classToIdMap[systemClass] ?: throw IllegalStateException("System type of $systemClass is not registered")

}