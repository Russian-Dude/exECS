package com.rdude.exECS.utils.reflection

import com.rdude.exECS.event.Event
import com.rdude.exECS.system.EventSystem
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

internal class EventSystemGenericQualifier {

    private val eventType = Event::class.createType()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> getEventArgument(system: EventSystem<T>) : KClass<T> {
        val eventSystemType = EventSystem::class.createType(listOf(KTypeProjection(null, null)))
        for (supertype in system::class.allSupertypes) {
            val arguments = supertype.arguments
            if (supertype.isSubtypeOf(eventSystemType) && arguments.size == 1 && arguments[0].type!!.isSubtypeOf(eventType)) {
                return arguments[0].type!!.classifier!! as KClass<T>
            }
        }
        throw IllegalStateException("Can not get generic argument for event")
    }

    fun getEventClassesForSystem(system: EventSystem<*>) : List<KClass<out Event>> {
        val mainClass = getEventArgument(system)
        return Package.getPackages()
            .asSequence()
            .map { it.name.substringBefore('.') }
            .distinct()
            .map { Reflections(it) }
            .flatMap { it.getSubTypesOf(mainClass.java) }
            .map { it.kotlin }
            .filterNot { it.isAbstract }
            .toMutableList()
            .apply { add(mainClass) }
    }

}