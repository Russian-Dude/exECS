package com.rdude.exECS.serialization

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.SingletonEntity
import kotlin.reflect.KClass

class SingletonSnapshot(val components: Map<KClass<out Component>, Component?>, val singletonEntity: SingletonEntity)