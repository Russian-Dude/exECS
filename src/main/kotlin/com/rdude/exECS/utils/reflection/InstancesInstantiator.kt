package com.rdude.exECS.utils.reflection

import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

object InstancesInstantiator {

    inline fun <reified T : Any> instantiateAll(list: List<KClass<out T>>): List<T> =
        instantiateAll(T::class, list)

    fun <T : Any> instantiateAll(mainType: KClass<T>, list: List<KClass<out T>>): List<T> {
        val instantiated: MutableMap<KClass<out T>, T> = HashMap(list.size)
        for (cl in list) {
            createInstanceAndEnclosingInstances(cl, instantiated, mainType, list)
        }
        return list.map { instantiated[it]!! }
    }

    private fun <T : Any> createInstanceAndEnclosingInstances(
        from: KClass<out T>,
        instantiated: MutableMap<KClass<out T>, T>,
        mainType: KClass<T>,
        mainList: List<KClass<out T>>
    ) {

        try {
            // if child inner class was instantiated before thus this class is likely instantiated too
            val instantiatedInstance = instantiated[from]
            if (instantiatedInstance != null) return

            val objectInstance = from.java.declaredFields.find { it.name == "INSTANCE" }
                ?.apply { isAccessible = true }
                ?.get(null)
                ?.let { from.cast(it) }
            if (objectInstance != null) {
                instantiated[from] = objectInstance
                return
            }

            val constructor =
                if (from.isInner) {
                    from.constructors
                        .find { c ->
                            c.valueParameters.size == 1 || c.valueParameters.drop(1).all { p -> p.isOptional }
                        }
                } else {
                    from.constructors
                        .find { c -> c.valueParameters.isEmpty() || c.valueParameters.all { p -> p.isOptional } }
                }

            if (constructor == null) throw IllegalStateException(
                "AutoRegistrar is unable create instance of the $from using the default instantiate function." +
                        " At least one constructor without non-optional arguments is required."
            )

            val constructorArgs =
                if (from.isInner) {
                    val enclosingCl = from.java.enclosingClass
                    if (enclosingCl.kotlin !in mainList) {
                        throw IllegalStateException("$from is inner but enclosing ${enclosingCl.kotlin} is not being registered. It was probably filtered out or has irrelevant type")
                    }
                    val enclosingInstance = instantiated[enclosingCl.kotlin]
                    if (enclosingInstance == null) {
                        @Suppress("UNCHECKED_CAST")
                        createInstanceAndEnclosingInstances(
                            enclosingCl.kotlin as KClass<out T>,
                            instantiated,
                            mainType,
                            mainList
                        )
                        arrayOf<Any?>(instantiated[enclosingCl.kotlin])
                    }
                    else arrayOf<Any?>(enclosingInstance)
                }
                else {
                    arrayOf<Any?>()
                }

            val initialAccess = constructor.isAccessible
            constructor.isAccessible = true
            val instance = constructor.call(*constructorArgs)
            constructor.isAccessible = initialAccess

            instantiated[from] = instance
        }
        catch (exception: Exception) {
            throw RuntimeException("Failed to auto-register $from", exception)
        }
    }

}