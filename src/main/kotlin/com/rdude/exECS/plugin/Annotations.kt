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


/** @param superType Event, Component etc
 * @param type concrete type (eg MyComponent) */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class GeneratedTypeIdProperty(val superType: KClass<*>, val type: KClass<*>)


@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class GeneratedDefaultPoolProperty(val type: KClass<out Poolable>)


/** If exECS compiler plugin is enabled, IR dump (after applying plugin) of an element annotated with this annotation
 * will be printed as a warning message at compile time.*/
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FUNCTION)
annotation class DebugIR


