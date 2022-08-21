package com.rdude.exECS.world

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.componentTypeId
import kotlin.reflect.KClass

class WorldUtils internal constructor(private val world: World) {


    fun getAllComponents(): List<Component> =
        world.entityMapper.componentMappers
            .flatMap { it.backingArray.asSequence() }
            .filterNotNull()


    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getAllComponentsOfType(type: KClass<T>): List<T> =
        (world.entityMapper.componentMappers[type.componentTypeId].backingArray as Array<T?>)
            .filterNotNull()
            .distinct()


    inline fun <reified T : Component> getAllComponentsOfType() = getAllComponentsOfType(T::class)


    fun getAllComponentsGroupedByType(): Map<KClass<out Component>, List<Component>> =
        world.entityMapper.componentMappers
            .associate {
                Pair(
                    ExEcs.componentTypeIDsResolver.typeById(it.componentTypeId),
                    it.backingArray.filterNotNull().distinct()
                )
            }


    fun getAllComponentsGroupedByEntities(): List<List<Component>> =
        (0..world.entityMapper.componentMappers.maxOf { it.backingArray.size })
            .map { index ->
                world.entityMapper.componentMappers.mapNotNull { mapper -> mapper.backingArray[index] }
            }
            .filter { it.isNotEmpty() }


    fun getAllSingletons(): List<SingletonEntity> = world.entityMapper.singletons.filterNotNull()


    fun getAllSystems(): List<System> = world.systems.filterNotNull()

}