package com.rdude.exECS.pool

import com.rdude.exECS.exception.DefaultPoolCanNotBeCreatedException

/** Annotated constructor of [Poolable] will be used to create new instances by the [default][fromPool] Pool..
 * @throws DefaultPoolCanNotBeCreatedException If constructor has non default arguments or more than one constructor of a class is annotated.*/
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CONSTRUCTOR)
annotation class ConstructorForDefaultPool
