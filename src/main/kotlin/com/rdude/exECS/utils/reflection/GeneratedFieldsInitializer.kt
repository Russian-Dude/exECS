package com.rdude.exECS.utils.reflection

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.plugin.GeneratedComponentMapperProperty
import com.rdude.exECS.plugin.GeneratedSingletonEntityProperty
import com.rdude.exECS.plugin.GeneratedSystemProperty
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.utils.singletonTypeId
import com.rdude.exECS.world.World
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

internal class GeneratedFieldsInitializer {

    private val classEntries = mutableMapOf<KClass<*>, ClassEntry<*>>()

    init {
        sequenceOf(System::class, SingletonEntity::class)
            .flatMap { ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(it) }
            .forEach { ownerClass ->

                var entry: ClassEntry<*>? = classEntries[ownerClass]

                for (field in ownerClass.java.declaredFields) {

                    // if field type is component mapper
                    val componentMapperAnnotation = field.getAnnotation(GeneratedComponentMapperProperty::class.java)
                    if (componentMapperAnnotation != null) {
                        if (entry == null) entry = ClassEntry.from(ownerClass)
                        entry.componentMapperFields[componentMapperAnnotation.componentType] = field
                        field.isAccessible = true
                        continue
                    }

                    // if field type is singleton entity
                    val singletonAnnotation = field.getAnnotation(GeneratedSingletonEntityProperty::class.java)
                    if (singletonAnnotation != null) {
                        if (entry == null) entry = ClassEntry.from(ownerClass)
                        val singletonType = singletonAnnotation.singletonType
                        entry.singletonEntitiesFields[singletonType] = field
                        val singletonEntry = classEntries.getOrPut(singletonType) { ClassEntry.from(singletonType) }
                        singletonEntry.dependentEntries.add(entry)
                        field.isAccessible = true
                        continue
                    }

                    // if field type is system
                    val systemAnnotation = field.getAnnotation(GeneratedSystemProperty::class.java)
                    if (systemAnnotation != null) {
                        if (entry == null) entry = ClassEntry.from(ownerClass)
                        val systemType = systemAnnotation.systemType
                        entry.systemFields[systemType] = field
                        val systemEntry = classEntries.getOrPut(systemType) { ClassEntry.from(systemType) }
                        systemEntry.dependentEntries.add(entry)
                        field.isAccessible = true
                    }
                }
                if (entry != null && !classEntries.containsKey(ownerClass)) {
                    classEntries[ownerClass] = entry
                }
            }
    }


    fun systemAdded(system: System, world: World) = added(system, world)

    fun systemRemoved(system: System, world: World) = removed(system, world)

    fun singletonEntityAdded(singletonEntity: SingletonEntity, world: World) = added(singletonEntity, world)

    fun singletonEntityRemoved(singletonEntity: SingletonEntity, world: World) = removed(singletonEntity, world)


    private fun added(instance: Any, world: World) {
        val entry = classEntries[instance::class] ?: return
        // init component mapper fields
        entry.componentMapperFields.forEach { (componentType, field) ->
            field.set(instance, world.entityMapper.componentMappers[componentType.componentTypeId])
        }
        // init singleton entities fields
        entry.singletonEntitiesFields.forEach { (singletonType, field) ->
            field.set(instance, world.entityMapper.singletons[singletonType.singletonTypeId])
        }
        // init system fields
        entry.systemFields.forEach { (systemType, field) ->
            field.set(instance, world.getSystem(systemType))
        }
        // init fields in dependent entries
        entry.dependentEntries.forEach { dependentEntry ->
            entry.setFieldOfDependentEntry(dependentEntry, world)
        }
    }

    private fun removed(instance: Any, world: World) {
        val entry = classEntries[instance::class] ?: return
        // clear component mapper fields
        entry.componentMapperFields.forEach { (_, field) ->
            field.set(instance, null)
        }
        // clear singleton entities fields
        entry.singletonEntitiesFields.forEach { (_, field) ->
            field.set(instance, null)
        }
        // clear system fields
        entry.systemFields.forEach { (_, field) ->
            field.set(instance, null)
        }
        // clear fields in dependent entries
        entry.dependentEntries.forEach { dependentEntry ->
            entry.clearFieldOfDependentEntry(dependentEntry, world)
        }
    }


    private abstract class ClassEntry<T : Any>(val kClass: KClass<out T>) {

        val componentMapperFields = mutableMapOf<KClass<out Component>, Field>()

        val systemFields = mutableMapOf<KClass<out System>, Field>()

        val singletonEntitiesFields = mutableMapOf<KClass<out SingletonEntity>, Field>()

        val dependentEntries = IterableArray<ClassEntry<*>>()

        abstract fun getInstance(world: World): T?

        abstract fun setFieldOfDependentEntry(entry: ClassEntry<*>, world: World)

        abstract fun clearFieldOfDependentEntry(entry: ClassEntry<*>, world: World)

        companion object {

            @Suppress("UNCHECKED_CAST")
            fun from(kClass: KClass<*>): ClassEntry<*> = when {
                kClass.isSubclassOf(System::class) -> SystemClassEntry(kClass as KClass<out System>)
                kClass.isSubclassOf(SingletonEntity::class) -> SingletonEntityClassEntry(kClass as KClass<out SingletonEntity>)
                else -> throw IllegalStateException("Creating of class entry of $kClass is not implemented")
            }

        }

    }


    private class SystemClassEntry(kClass: KClass<out System>) : ClassEntry<System>(kClass) {

        override fun getInstance(world: World): System? = world.getSystem(kClass)

        override fun setFieldOfDependentEntry(entry: ClassEntry<*>, world: World) {
            val dependentInstance = entry.getInstance(world) ?: return
            entry.systemFields[kClass]!!.set(dependentInstance, this.getInstance(world))
        }

        override fun clearFieldOfDependentEntry(entry: ClassEntry<*>, world: World) {
            val dependentInstance = entry.getInstance(world) ?: return
            entry.systemFields[kClass]!!.set(dependentInstance, null)
        }
    }


    private class SingletonEntityClassEntry(kClass: KClass<out SingletonEntity>) : ClassEntry<SingletonEntity>(kClass) {

        override fun getInstance(world: World): SingletonEntity? = world.entityMapper.singletons[kClass.singletonTypeId]

        override fun setFieldOfDependentEntry(entry: ClassEntry<*>, world: World) {
            val dependentInstance = entry.getInstance(world) ?: return
            entry.singletonEntitiesFields[kClass]!!.set(dependentInstance, this.getInstance(world))
        }

        override fun clearFieldOfDependentEntry(entry: ClassEntry<*>, world: World) {
            val dependentInstance = entry.getInstance(world) ?: return
            entry.singletonEntitiesFields[kClass]!!.set(dependentInstance, null)
        }
    }

}