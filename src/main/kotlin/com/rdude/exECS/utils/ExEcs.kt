package com.rdude.exECS.utils

import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.entity.SingletonEntityIDsResolver
import com.rdude.exECS.event.EventTypeIDsResolver
import com.rdude.exECS.pool.DefaultPools
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.reflection.EventSystemGenericQualifier
import com.rdude.exECS.utils.reflection.GeneratedFieldsInitializer
import com.rdude.exECS.utils.reflection.ReflectionUtils
import com.rdude.exECS.utils.reflection.SystemDelegatesUpdater
import kotlin.reflect.KClass

internal object ExEcs {

    private var initialized = false

    lateinit var reflectionUtils: ReflectionUtils private set

    lateinit var defaultPools: DefaultPools private set

    lateinit var componentTypeIDsResolver: ComponentTypeIDsResolver private set

    lateinit var eventTypeIDsResolver: EventTypeIDsResolver private set

    lateinit var singletonEntityIDsResolver: SingletonEntityIDsResolver private set

    lateinit var generatedFieldsInitializer: GeneratedFieldsInitializer private set

    lateinit var eventSystemGenericQualifier: EventSystemGenericQualifier private set

    lateinit var systemDelegatesUpdater: SystemDelegatesUpdater private set

    fun initializeIfNeeded() {
        if (initialized) return
        reflectionUtils = ReflectionUtils()
        defaultPools = DefaultPools()
        componentTypeIDsResolver = ComponentTypeIDsResolver()
        eventTypeIDsResolver = EventTypeIDsResolver()
        singletonEntityIDsResolver = SingletonEntityIDsResolver()
        generatedFieldsInitializer = GeneratedFieldsInitializer()
        eventSystemGenericQualifier = EventSystemGenericQualifier()
        systemDelegatesUpdater = SystemDelegatesUpdater()
        registerGeneratedPoolsAsDefaults()
        initialized = true
    }


    private fun registerGeneratedPoolsAsDefaults() {

        val toPoolName: (KClass<out Poolable>) -> String =
            { "execs_generated_pool_for_${it.qualifiedName?.replace(".", "_")}" }

        reflectionUtils.getNotAbstractSubClassesFromAllPackages(Poolable::class)
            .filter { it.java.fields.any { field -> field.name == toPoolName.invoke(it) } }
            .forEach { poolableCl ->
                val field = poolableCl.java.getField(toPoolName.invoke(poolableCl))
                field.isAccessible = true
                val pool = field.get(null) as Pool<Poolable>
                defaultPools[poolableCl] = pool
            }
    }

}