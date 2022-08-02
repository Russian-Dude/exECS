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

    @JvmField val reflectionUtils: ReflectionUtils = ReflectionUtils()

    @JvmField val defaultPools: DefaultPools = DefaultPools()

    @JvmField val componentTypeIDsResolver: ComponentTypeIDsResolver = ComponentTypeIDsResolver()

    @JvmField val eventTypeIDsResolver: EventTypeIDsResolver = EventTypeIDsResolver()

    @JvmField val systemTypeIDsResolver: SystemTypeIdResolver = SystemTypeIdResolver()

    @JvmField val singletonEntityIDsResolver: SingletonEntityIDsResolver = SingletonEntityIDsResolver()

    @JvmField val generatedFieldsInitializer: GeneratedFieldsInitializer = GeneratedFieldsInitializer()

    @JvmField val eventSystemGenericQualifier: EventSystemGenericQualifier = EventSystemGenericQualifier()

    @JvmField val aspectCorrectnessChecker: AspectCorrectnessChecker = AspectCorrectnessChecker()

}