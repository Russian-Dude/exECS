package com.rdude.exECS.utils.reflection

import kotlin.reflect.KClass

/** This annotation is used by the [AutoRegistrar]. Annotated class will be registered before the provided [classes].*/
@Target(AnnotationTarget.CLASS)
annotation class Before(vararg val classes: KClass<*>)

/** This annotation is used by the [AutoRegistrar]. Annotated class will be registered after the provided [classes].*/
@Target(AnnotationTarget.CLASS)
annotation class After(vararg val classes: KClass<*>)