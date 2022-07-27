package com.rdude.exECS.plugin

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.system.System
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class GeneratedComponentMapperProperty(val componentType: KClass<out Component>)


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class GeneratedSingletonEntityProperty(val singletonType: KClass<out SingletonEntity>)


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class GeneratedSystemProperty(val systemType: KClass<out System>)


/** @param type either Component or Event etc. (class simple name).*/
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class GeneratedTypeIdProperty(val type: String)


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class GeneratedDefaultPoolProperty


