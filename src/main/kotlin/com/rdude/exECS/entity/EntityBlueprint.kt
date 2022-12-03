package com.rdude.exECS.entity

import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.component.Component
import com.rdude.exECS.world.World

/** Blueprints are used to create predefined [Components][Component] compositions with optional parent-child
 * relations between [Entities][Entity].
 *
 * Entities then can be created from Blueprints using ``entity.``[createEntity][com.rdude.exECS.world.WorldAccessor.createEntity]``(blueprint)`` method.
 *
 * More info can be found on the [wiki](https://github.com/Russian-Dude/exECS/wiki/Entity#blueprints)*/
class EntityBlueprint<T : EntityBlueprintConfiguration> @PublishedApi internal constructor(
    @JvmField
    @PublishedApi
    internal val defaultConfiguration: () -> T,

    @JvmField
    @PublishedApi
    internal val fullBuildingFun: (World, EntityBuilder<T>, T) -> Int,

    @JvmField
    internal val endBuildingFun: (EntityBuilder<T>, T) -> Unit
) {


    companion object {

        operator fun invoke(applyBuilder: EntityBuilder<*>.() -> Unit): EntityBlueprint<*> {
            val fullBuildingFun: (World, EntityBuilder<EntityBlueprintConfiguration.NoConfiguration>, EntityBlueprintConfiguration.NoConfiguration) -> Int =
                { world, builder, _ ->
                    val id = builder.startBuilding(world) { EntityBlueprintConfiguration.NoConfiguration }
                    builder.endBuilding(applyBuilder)
                    id
                }
            val endBuildingFun: (EntityBuilder<EntityBlueprintConfiguration.NoConfiguration>, EntityBlueprintConfiguration.NoConfiguration) -> Unit =
                { builder, _ ->
                    builder.endBuilding(applyBuilder)
                }
            return EntityBlueprint(
                EntityBlueprintConfiguration.noConfigurationProducer,
                fullBuildingFun,
                endBuildingFun
            )
        }

        inline operator fun <reified T> invoke(noinline applyBuilder: EntityBuilder<T>.(config: T) -> Unit): EntityBlueprint<T> where T : EntityBlueprintConfiguration, T : Poolable =
            invoke({ fromPool<T>() }, applyBuilder)

        inline operator fun <reified T : EntityBlueprintConfiguration> invoke(
            noinline defaultConfiguration: () -> T,
            noinline applyBuilder: EntityBuilder<T>.(config: T) -> Unit
        ): EntityBlueprint<T> {
            val fullBuildingFun: (World, EntityBuilder<T>, T) -> Int = { world, builder, configuration ->
                val id = builder.startBuilding(world, defaultConfiguration)
                builder.endBuilding(configuration, applyBuilder)
                id
            }
            val endBuildingFun: (EntityBuilder<T>, T) -> Unit = { builder, configuration ->
                builder.endBuilding(configuration, applyBuilder)
            }
            return EntityBlueprint(
                defaultConfiguration,
                fullBuildingFun,
                endBuildingFun
            )
        }

    }
}