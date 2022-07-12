package com.rdude.exECS.pool

import com.rdude.exECS.exception.DefaultPoolCanNotBeCreatedException
import com.rdude.exECS.exception.DefaultPoolNotExistException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

internal class DefaultPools {

    /** This map is used to store Pools only for those Poolable subclasses that were compiled without exECS plugin.*/
    private val defaultPools: MutableMap<KClass<out Poolable>, Pool<Poolable>?> = HashMap()

    /** This map is used to store Pools only for those Poolable subclasses instances that were compiled without exECS plugin.*/
    internal val customPools: MutableMap<Poolable, Pool<Poolable>?> = HashMap()

    internal operator fun get(kClass: KClass<out Poolable>) = defaultPool(kClass) ?: throw DefaultPoolNotExistException(kClass)

    internal operator fun set(kClass: KClass<out Poolable>, pool: Pool<Poolable>) = defaultPools.put(kClass, pool)

    /** Get default [Pool] for requested Poolable type.
     * @throws DefaultPoolCanNotBeCreatedException
     * @throws DefaultPoolNotExistException*/
    internal inline fun <reified T : Poolable> defaultPool() = defaultPool(T::class)

    /** Get default [Pool] for requested Poolable type.
     * @throws DefaultPoolCanNotBeCreatedException
     * @throws DefaultPoolNotExistException*/
    internal fun <T : Poolable> defaultPool(kClass: KClass<T>) = defaultPools.getOrPut(kClass) {
        val constructor = findConstructor(kClass) ?: throw DefaultPoolNotExistException(kClass)
        Pool { constructor.callBy(emptyMap()) }
    }


    private fun <T : Poolable> findConstructor(forPoolable: KClass<T>): KFunction<T>? {
        if (forPoolable.isInner) {
            throw DefaultPoolCanNotBeCreatedException("Can not create default pool for inner $forPoolable. " +
                    "Class should not be inner to have the default pool")
        }

        var constructor: KFunction<T>? = null

        // prefer constructor annotated with @ConstructorForDefaultPool
        val annotatedConstructors = forPoolable.constructors
            .filter { it.hasAnnotation<ConstructorForDefaultPool>() }
        if (annotatedConstructors.size == 1) {
            val aConstructor = annotatedConstructors[0]
            val valueParameters = aConstructor.valueParameters
            if (valueParameters.isNotEmpty() && valueParameters.any { !it.isOptional }) {
                throw DefaultPoolCanNotBeCreatedException(
                    "Constructor annotated with @ConstructorForDefaultPool annotation in class " +
                            "$forPoolable must have no arguments or all arguments must be optional"
                )
            }
            constructor = aConstructor
        } else if (annotatedConstructors.size > 1) {
            throw DefaultPoolCanNotBeCreatedException(
                "Only one constructor per class can be annotated with @ConstructorForDefaultPool annotation. " +
                        "${annotatedConstructors.size} constructors with this annotation found in $forPoolable"
            )
        }

        // then prefer primary constructor
        if (constructor == null && forPoolable.primaryConstructor != null) {
            val valueParameters = forPoolable.primaryConstructor!!.valueParameters
            if (valueParameters.isEmpty() || valueParameters.all { it.isOptional }) {
                constructor = forPoolable.primaryConstructor!!
            }
        }

        // then any other with no args or with all optional args
        if (constructor == null) {
            constructor = forPoolable.constructors
                    .firstOrNull { con -> con.valueParameters.isEmpty() || con.valueParameters.all { it.isOptional } }
        }

        constructor?.isAccessible = true

        return constructor
    }

}