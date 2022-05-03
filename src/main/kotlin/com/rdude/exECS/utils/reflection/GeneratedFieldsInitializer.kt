package com.rdude.exECS.utils.reflection

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.IdFactory
import com.rdude.exECS.world.World
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.isSubclassOf

internal class GeneratedFieldsInitializer {

    fun initialize(system: System) {
        initializeGeneratedComponentMappers(system) { it.world }
    }

    fun initialize(singletonEntity: SingletonEntity) {
        initializeGeneratedComponentMappers(singletonEntity) { it.world }
    }

    // The enum's companion object properties are not initialized at the time the enum entry is initialized,
    // so property declared in entry can not access factory inside companion object during initialization.
    // Because of that, have to initialize them here
    fun initializeAllEnumComponentIds() {
        ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(Component::class)
            .filter { it.isSubclassOf(Enum::class) }
            .forEach { enumComponent ->
                enumComponent.java.enumConstants.forEachIndexed { index, constant ->
                    val field = enumComponent.java.fields.find { it.name == "componentId" }!!
                    field.set(constant, index)
                }
            }
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