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
        // not component related:
        classToIdMap[ActingEvent::class] = ACTING_EVENT_ID
        classToIdMap[EntityAddedEvent::class] = ENTITY_ADDED_ID
        classToIdMap[EntityRemovedEvent::class] = ENTITY_REMOVED_ID
        classToIdMap[ChildEntityAddedEvent::class] = CHILD_ENTITY_ADDED_ID
        classToIdMap[ChildEntityRemovedEvent::class] = CHILD_ENTITY_REMOVED_ID

        // component related events ids: notComponentRelatedAmount + (offset * componentsAmount) + componentId
        // currently there are 3 component related events: added (offset = 0), removed(offset = 1) and changed(offset = 2)
        val componentRelatedEventsIdsAmount = ExEcs.componentTypeIDsResolver.size * INTERNAL_COMPONENT_RELATED_EVENTS_AMOUNT
        return INTERNAL_NON_COMPONENT_RELATED_EVENTS_AMOUNT + componentRelatedEventsIdsAmount
    }

    companion object {
        const val INTERNAL_NON_COMPONENT_RELATED_EVENTS_AMOUNT = 5
        const val INTERNAL_COMPONENT_RELATED_EVENTS_AMOUNT = 3
        const val ACTING_EVENT_ID = 0
        const val ENTITY_ADDED_ID = 1
        const val ENTITY_REMOVED_ID = 2
        const val CHILD_ENTITY_ADDED_ID = 3
        const val CHILD_ENTITY_REMOVED_ID = 4
        const val COMPONENT_ADDED_ID_SHIFT = 0
        const val COMPONENT_REMOVED_ID_SHIFT = 1
        const val COMPONENT_CHANGED_ID_SHIFT = 2
    }

}