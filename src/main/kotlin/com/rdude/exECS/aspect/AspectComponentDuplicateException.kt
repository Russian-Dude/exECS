package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import kotlin.reflect.KClass

internal class AspectComponentDuplicateException(val duplicate: KClass<out Component>) : Exception()