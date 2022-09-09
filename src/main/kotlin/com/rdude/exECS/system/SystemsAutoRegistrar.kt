package com.rdude.exECS.system

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters


class SystemsAutoRegistrar {

    /** Function that changes list of founded [System] classes.*/
    var classesFilter: (List<KClass<out System>>) -> List<KClass<out System>> = { it }


    /** Function that produce new [System] instance by given KClass.*/
    var instanceProducer: (KClass<out System>) -> System = {
        val constructor = it.constructors
            .first { c -> c.valueParameters.isEmpty() || c.valueParameters.all { p -> p.isOptional }}
        constructor.call()
    }


    internal fun register(world: World) {
        var classes = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(System::class).toList()
        classes = classesFilter.invoke(classes)
        classes
            .map { instanceProducer.invoke(it) }
            .forEach { world.registerSystem(it) }
    }


    internal companion object {

        val defaultRegistrar = SystemsAutoRegistrar()

    }
}