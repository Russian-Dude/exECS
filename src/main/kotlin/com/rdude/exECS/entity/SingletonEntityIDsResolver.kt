package com.rdude.exECS.entity

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

internal class SingletonEntityIDsResolver {

    private val typeToId = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(SingletonEntity::class)
        .withIndex()
        .associate { (index, type) -> type to index }

    private val idToType = typeToId
        .map { (type, id) -> id to type }
        .toMap()

    val size = typeToId.size

    fun idFor(type: KClass<out SingletonEntity>) =
        typeToId[type] ?: throw IllegalArgumentException("Singleton of type $type is not registered")

    fun getType(id: Int) = idToType[id] ?: throw IllegalArgumentException("Singleton with type id $id is not registered")

}