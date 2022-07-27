package com.rdude.exECS.exception

import com.rdude.exECS.system.System
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor

/** Thrown when [World] related methods in the [WorldAccessor] access [WorldAccessor.world] property and
 * this property is null. For example, this can happen when a [System] or [SingletonEntity] is not registered in the World.*/
class WorldNotSetException(accessor: WorldAccessor) : ExEcsException(
    "Can not access World from ${accessor::class}. ${getReason(accessor)}"
) {

    private companion object {

        private fun getReason(accessor: WorldAccessor): String = when (accessor) {
            is System -> "System is not registered in a World"
            else -> "World property is null"
        }

    }

}