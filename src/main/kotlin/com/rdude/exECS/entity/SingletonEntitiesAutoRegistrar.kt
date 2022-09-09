package com.rdude.exECS.entity

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import kotlin.reflect.KClass
import kotlin.reflect.full.valueParameters

class SingletonEntitiesAutoRegistrar {

    /** Function that changes list of founded [SingletonEntity] classes.*/
    var classesFilter: (List<KClass<out SingletonEntity>>) -> List<KClass<out SingletonEntity>> = { it }


    /** Function that produce new [SingletonEntity] instance by given KClass.*/
    var instanceProducer: (KClass<out SingletonEntity>) -> SingletonEntity = {
        val constructor = it.constructors
            .first { c -> c.valueParameters.isEmpty() || c.valueParameters.all { p -> p.isOptional }}
        constructor.call()
    }


    internal fun register(world: World) {
        var classes = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(SingletonEntity::class).toList()
        classes = classesFilter.invoke(classes)
        classes
            .map { instanceProducer.invoke(it) }
            .forEach { world.addSingletonEntity(it) }
    }


    internal companion object {

        val defaultRegistrar = SingletonEntitiesAutoRegistrar()

    }

}