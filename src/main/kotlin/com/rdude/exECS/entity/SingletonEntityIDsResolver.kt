package com.rdude.exECS.entity

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

internal class SingletonEntityIDsResolver {

    private val typeToId = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(SingletonEntity::class)
        .withIndex()
        .associate { (index, type) -> type to index }

    val typesAmount = typeToId.size

    fun getId(type: KClass<out SingletonEntity>) =
        typeToId[type] ?: throw IllegalArgumentException("Singleton of type $type is not registered")

}