package com.rdude.exECS.utils.reflection

import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs

internal class GeneratedFieldsInitializer {

    fun initialize(system: System) {
        // component mappers
        system::class.java.fields
            .filter { it.type == ComponentMapper::class.java }
            .forEach {
                it.isAccessible = true
                val componentTypeFqName = it.name
                    .replaceFirst("generated_component_mapper_for_", "")
                    .replace("_", ".")
                val componentTypeId = ExEcs.componentTypeIDsResolver.idFor(componentTypeFqName)
                it.set(system, system.world.entityMapper.componentMappers[componentTypeId])
            }
    }

}