package com.rdude.exECS.component

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

class ComponentTypeIDsResolver {

    internal val size: Int
    private val classToIdMap: MutableMap<KClass<out Component>, Int> = HashMap()
    private val idToKClass: MutableMap<Int, KClass<out Component>> = HashMap()

    init {
        val allComponentClasses = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(Component::class)
        for ((index, kClass) in allComponentClasses.withIndex()) {
            classToIdMap[kClass] = index
            idToKClass[index] = kClass
        }
        size = allComponentClasses.size
    }

    fun idFor(componentClass: KClass<out Component>): Int =
        classToIdMap[componentClass] ?: throw IllegalStateException("Component ${componentClass.qualifiedName} is not registered")


    fun typeById(id: Int): KClass<out Component> = idToKClass[id] ?: throw IllegalStateException("Component with type id $id is not registered")

}