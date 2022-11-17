package com.rdude.exECS.utils.reflection

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters

class AutoRegistrar<E : Any, R : Any> private constructor(
    private val elementCl: KClass<E>,
    private val registrationFun: (E, R) -> Unit
) : AutoRegistrarProperties<E, R> {

    /** Function that transforms a list of founded classes.
     * New instances will be created and registered in the order of their classes in this list.*/
    override var classesFilter: (List<KClass<out E>>) -> List<KClass<out E>> = { it }

    /** Function that produces new instance by given KClass.*/
    override var instanceProducer: (KClass<out E>) -> E = {
        it.objectInstance
            ?: it.constructors
                .find { c -> c.valueParameters.isEmpty() || c.valueParameters.all { p -> p.isOptional } }
                ?.call()
            ?: throw IllegalStateException(
                "AutoRegistrar is unable not create instance of the $it using default instanceProducer function." +
                        " At least one constructor with all optional arguments is required."
            )
    }


    fun register(registry: R) =
        ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(elementCl)
            .let { classesFilter.invoke(it) }
            .map { instanceProducer.invoke(it) }
            .forEach { registrationFun.invoke(it, registry) }


    companion object {

        operator fun <E : Any, R : Any> invoke(
            elementCl: KClass<E>,
            registrationFun: (E, R) -> Unit
        ): AutoRegistrar<E, R> = AutoRegistrar(elementCl, registrationFun)

        inline operator fun <reified E : Any, R : Any> invoke(noinline registrationFun: (E, R) -> Unit) =
            invoke(E::class, registrationFun)

    }

}