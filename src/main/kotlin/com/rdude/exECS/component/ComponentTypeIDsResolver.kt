package com.rdude.exECS.component

import com.rdude.exECS.plugin.GeneratedTypeIdProperty
import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class ComponentTypeIDsResolver {

    internal val size: Int
    private val fqNameToId: MutableMap<String, Int> = HashMap()
    private val classToIdMap: MutableMap<KClass<out Component>, Int> = HashMap()
    private val idToKClass: MutableMap<Int, KClass<out Component>> = HashMap()

    init {
        val allComponentClasses = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(Component::class)
        for ((index, kClass) in allComponentClasses.withIndex()) {
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

    fun typeById(id: Int): KClass<out Component> = idToKClass[id] ?: throw IllegalStateException("Component with type id $id is not registered")

    private fun initCompanionIdField(kClass: KClass<out Component>, id: Int) {
        for (field in kClass.java.fields) {
            val annotation = field.getAnnotation(GeneratedTypeIdProperty::class.java) ?: continue
            if (annotation.type != Component::class.simpleName) continue
            field.isAccessible = true
            field.set(null, id)
        }
    }

}