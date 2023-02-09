package com.rdude.exECS.utils.reflection

import kotlin.reflect.KClass

/** Describes every step [AutoRegistrar] performs during the registration process.
 * The order of the steps: [find], [filter], [order], [instantiate].
 * @property find function that produces the list of classes to register. Default implementation scan the classpath
 * looking for required classes.
 * @property filter function that filters found classes. Default implementation do nothing.
 * @property order function that transforms the list of filtered classes to an ordered list of classes.
 * Default implementation sort classes relying on [Before] and [After] annotations.
 * @property instantiate function that creates instances from classes.
 * Default implementation creates instances using reflection (including non-public and inner classes).
 * @param E elements to be registered
 * @param R registry in which [E] elements will be registered*/
sealed interface AutoRegistrarProperties<E : Any, R : Any> {

    var find: () -> List<KClass<out E>>

    var filter: (KClass<out E>) -> Boolean

    var order: (List<KClass<out E>>) -> List<KClass<out E>>

    // intentionally list -> list and not class -> instance. For easier instantiation of inner classes
    var instantiate: (List<KClass<out E>>) -> List<E>
}