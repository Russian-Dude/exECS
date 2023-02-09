package com.rdude.exECS.utils.reflection

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass

/** Finds, filters, orders, instantiates and registers elements in the provided registry.
 * @param E elements to be registered
 * @param R registry in which [E] elements will be registered*/
class AutoRegistrar<E : Any, R : Any> private constructor(
    private val elementCl: KClass<E>,
    private val registrationFun: (E, R) -> Unit
) : AutoRegistrarProperties<E, R> {


    override var find: () -> List<KClass<out E>> = {
        ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(elementCl)
    }

    override var filter: (KClass<out E>) -> Boolean = { true }

    override var order: (List<KClass<out E>>) -> List<KClass<out E>> = {
        AutoRegisterOrders.byAnnotations(it)
    }

    override var instantiate: (List<KClass<out E>>) -> List<E> = {
        InstancesInstantiator.instantiateAll(elementCl, it)
    }


    fun register(registry: R) =
        find.invoke()
            .filter(filter)
            .let(order)
            .let(instantiate)
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