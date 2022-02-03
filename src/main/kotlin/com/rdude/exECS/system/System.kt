package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.utils.collections.EntityIDIterableArray
import com.rdude.exECS.world.World

abstract class System : Iterable<EntityID> {

    abstract val aspect: Aspect
    lateinit var world: World
    internal val entityIDs = EntityIDIterableArray()

    override operator fun iterator(): Iterator<EntityID> = entityIDs.iterator()

    internal fun addEntity(entity: EntityID) = entityIDs.add(entity)

    internal fun removeEntity(entity: EntityID, replacedBy: EntityID) = entityIDs.remove(entity, replacedBy)

    internal fun removeEntity(entity: EntityID) = entityIDs.remove(entity)

    fun createEntity(vararg components: Component) = world.createEntity(*components)
}