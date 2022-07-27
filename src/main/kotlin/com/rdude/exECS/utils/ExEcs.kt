package com.rdude.exECS.utils

import com.rdude.exECS.aspect.AspectCorrectnessChecker
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.entity.SingletonEntityIDsResolver
import com.rdude.exECS.event.EventTypeIDsResolver
import com.rdude.exECS.pool.DefaultPools
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.system.SystemTypeIdResolver
import com.rdude.exECS.utils.reflection.EventSystemGenericQualifier
import com.rdude.exECS.utils.reflection.GeneratedFieldsInitializer
import com.rdude.exECS.utils.reflection.ReflectionUtils
import kotlin.reflect.KClass

internal object ExEcs {

    private var initialized = false

    lateinit var reflectionUtils: ReflectionUtils private set

    lateinit var defaultPools: DefaultPools private set

    lateinit var componentTypeIDsResolver: ComponentTypeIDsResolver private set

    lateinit var eventTypeIDsResolver: EventTypeIDsResolver private set

    lateinit var systemTypeIDsResolver: SystemTypeIdResolver private set

    lateinit var singletonEntityIDsResolver: SingletonEntityIDsResolver private set

    lateinit var generatedFieldsInitializer: GeneratedFieldsInitializer private set

    lateinit var eventSystemGenericQualifier: EventSystemGenericQualifier private set

    lateinit var aspectCorrectnessChecker: AspectCorrectnessChecker

    init {
        initializeIfNeeded()
    }

    fun initializeIfNeeded() {
        if (initialized) return
        reflectionUtils = ReflectionUtils()
        defaultPools = DefaultPools()
        componentTypeIDsResolver = ComponentTypeIDsResolver()
        eventTypeIDsResolver = EventTypeIDsResolver()
        systemTypeIDsResolver = SystemTypeIdResolver()
        singletonEntityIDsResolver = SingletonEntityIDsResolver()
        generatedFieldsInitializer = GeneratedFieldsInitializer()
        eventSystemGenericQualifier = EventSystemGenericQualifier()
        aspectCorrectnessChecker = AspectCorrectnessChecker()
        initialized = true
    }

}