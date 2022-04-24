package com.rdude.exECS.utils.reflection

import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World

internal class GeneratedFieldsInitializer {

    fun initialize(system: System) {
        initializeGeneratedComponentMappers(system) { it.world }
    }

    fun initialize(singletonEntity: SingletonEntity) {
        initializeGeneratedComponentMappers(singletonEntity) { it.world }
    }

    private inline fun <T : Any> initializeGeneratedComponentMappers(instance: T, getWorld: (T) -> World) {
        instance::class.java.fields
            .filter { it.type == ComponentMapper::class.java && it.name.startsWith("generated_component_mapper_for_") }
            .forEach {
                it.isAccessible = true
                val componentTypeFqName = it.name
                    .replaceFirst("generated_component_mapper_for_", "")
                    .replace("_", ".")
                val componentTypeId = ExEcs.componentTypeIDsResolver.idFor(componentTypeFqName)
                it.set(instance, getWorld.invoke(instance).entityMapper.componentMappers[componentTypeId])
            }
    }

}