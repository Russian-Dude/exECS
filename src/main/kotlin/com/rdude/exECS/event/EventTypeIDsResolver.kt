package com.rdude.exECS.event

import com.rdude.exECS.utils.ExEcs
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

internal class EventTypeIDsResolver {

    private val classToIdMap: Map<KClass<out Event>, Int>
    internal val size: Int

    init {
        val classToIdMap: MutableMap<KClass<out Event>, Int> = HashMap()

        val allNotInternalEventClasses = ExEcs.reflectionUtils.getNotAbstractSubClassesFromAllPackages(Event::class)
            .filterNot { it.isSubclassOf(InternalEvent::class) }
        val internalEventsSize = resolveInternalEventsIds(classToIdMap)

        size = allNotInternalEventClasses.size + internalEventsSize

        classToIdMap += allNotInternalEventClasses
            .onEachIndexed { index, kClass -> initCompanionIdField(kClass, index + internalEventsSize) }
            .mapIndexed { index, kClass -> Pair(kClass, index + internalEventsSize) }
            .toMap()

        this.classToIdMap = classToIdMap
    }


    fun idFor(eventClass: KClass<out Event>): Int =
        classToIdMap[eventClass] ?: throw IllegalStateException("Component $eventClass is not registered")


    // hardcoded ids for internal events
    // return amount of internal event types
    private fun resolveInternalEventsIds(classToIdMap: MutableMap<KClass<out Event>, Int>): Int {
        classToIdMap[ComponentAddedEvent::class] = 0
        classToIdMap[ComponentRemovedEvent::class] = 1
        classToIdMap[ComponentChangedEvent::class] = 2
        classToIdMap[ActingEvent::class] = 3
        classToIdMap[EntityAddedEvent::class] = 4
        classToIdMap[EntityRemovedEvent::class] = 5
        return 6
    }


    private fun initCompanionIdField(kClass: KClass<out Event>, id: Int) {
        kClass.java.fields.singleOrNull {
            it.name == "execs_generated_event_type_id_property_for_${
                kClass.qualifiedName!!.replace(".", "_")}"
        }
            ?.let {
                it.isAccessible = true
                it.set(null, id)
            }
    }

}