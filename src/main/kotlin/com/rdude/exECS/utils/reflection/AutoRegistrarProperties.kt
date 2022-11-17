package com.rdude.exECS.utils.reflection

import kotlin.reflect.KClass

interface AutoRegistrarProperties<E : Any, R : Any> {

    var classesFilter: (List<KClass<out E>>) -> List<KClass<out E>>

    var instanceProducer: (KClass<out E>) -> E

}