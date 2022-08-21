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
            .mapIndexed { index, kClass -> Pair(kClass, index + internalEventsSize) }
            .toMap()

        this.classToIdMap = classToIdMap
    }


    fun idFor(eventClass: KClass<out Event>): Int =
        classToIdMap[eventClass] ?: throw IllegalStateException("Event type of $eventClass is not registered")


    // hardcoded ids for internal events
    // return amount of internal event types
    private fun resolveInternalEventsIds(classToIdMap: MutableMap<KClass<out Event>, Int>): Int {
        // 3 not component related: acting, entityAdded, entityRemoved
        classToIdMap[ActingEvent::class] = 0
        classToIdMap[EntityAddedEvent::class] = 1
        classToIdMap[EntityRemovedEvent::class] = 2

        // component related events ids: notComponentRelatedAmount + (offset * componentsAmount) + componentId
        // currently there are 3 component related events: added (offset = 0), removed(offset = 1) and changed(offset = 2)
        val componentRelatedEventsIdsAmount = ExEcs.componentTypeIDsResolver.size * 3
        return 3 + componentRelatedEventsIdsAmount
    }

}