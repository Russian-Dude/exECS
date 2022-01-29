package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.utils.collections.IterableList

abstract class System : Iterable<Entity> {

    abstract val aspect: Aspect
    val entities = IterableList<Entity>()

    override operator fun iterator(): Iterator<Entity> = entities.iterator()

    fun addEntity(entity: Entity) = entities.add(entity)

    fun removeEntity(entity: Entity) = entities.remove(entity)
}