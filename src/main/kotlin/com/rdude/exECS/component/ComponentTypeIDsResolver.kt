package com.rdude.exECS.component

import com.rdude.exECS.utils.reflection.ReflectionUtils
import kotlin.reflect.KClass

object ComponentTypeIDsResolver {

    internal val size: Int
    private val fqNameToId: MutableMap<String, Int> = HashMap()
    private val classToIdMap: MutableMap<KClass<out Component>, Int> = HashMap()
    private val idToKClass: MutableMap<Int, KClass<out Component>> = HashMap()

    init {
        val allComponentClasses = ReflectionUtils.getNotAbstractSubClassesFromAllPackages(Component::class)
        for ((index, kClass) in allComponentClasses.toList().withIndex()) {
            fqNameToId[kClass.qualifiedName!!] = index
            classToIdMap[kClass] = index
            idToKClass[index] = kClass
            initCompanionIdField(kClass, index)
        }
        size = allComponentClasses.size
    }

    fun idFor(componentClass: KClass<out Component>): Int =
        classToIdMap[componentClass] ?: throw IllegalStateException("Component ${componentClass.qualifiedName} is not registered")

    fun idFor(fqName: String): Int = fqNameToId[fqName] ?: throw IllegalStateException("Component $fqName is not registered")

    fun typeById(id: Int): KClass<out Component> = idToKClass[id] ?: throw IllegalStateException("Component with id $id is not registered")

    private fun initCompanionIdField(kClass: KClass<out Component>, id: Int) {
        kClass.java.fields.singleOrNull {
            it.name == "execs_generated_component_type_id_property_for_${
                kClass.qualifiedName!!.replace(".", "_")}"
        }
            ?.let {
                it.isAccessible = true
                it.set(null, id)
            }
    }

}