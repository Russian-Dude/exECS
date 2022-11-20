package com.rdude.exECS.serialization

import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.component.ParentEntityComponent
import com.rdude.exECS.utils.collections.IntIterableArray
import com.rdude.exECS.world.World

// data: ... parent, childrenAmount, children ...
class EntitiesParentChildRelationsSnapshot(val data: IntArray) {

    companion object {

        fun fromWorld(world: World) = fromComponentMappers(world.entityMapper.parentEntityComponents)

        internal fun fromComponentMappers(parentMapper: ComponentMapper<ParentEntityComponent>): EntitiesParentChildRelationsSnapshot {
            val result = IntIterableArray()
            for (component in parentMapper.backingArray) {
                if (component == null) continue
                result.add(component.entityId)
                result.add(component.children.size)
                component.children.forEach { result.add(it.id) }
            }
            return EntitiesParentChildRelationsSnapshot(result.backingArray.copyOf(result.size))
        }

    }

}