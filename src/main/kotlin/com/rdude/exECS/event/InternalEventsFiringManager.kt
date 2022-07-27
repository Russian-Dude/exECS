package com.rdude.exECS.event

import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.eventsTypesSubscription
import com.rdude.exECS.world.World
import kotlin.reflect.full.isSubclassOf

/** Manages whether internal events should be fired.
 * Events should be fired only if any of the registered event systems is interested in these events.
 *
 * Manages triggering of:
 * - [ComponentAddedEvent]
 * - [ComponentRemovedEvent]
 * - [ComponentChangedEvent]
 * - [EntityAddedEvent]
 * - [EntityRemovedEvent]*/
internal class InternalEventsFiringManager(val world: World) {

    val componentAddedSubscribersAmount = IntArray(ExEcs.componentTypeIDsResolver.size)
    val componentRemovedSubscribersAmount = IntArray(ExEcs.componentTypeIDsResolver.size)
    val componentChangedSubscribersAmount = IntArray(ExEcs.componentTypeIDsResolver.size)
    var entityAddedSubscribersAmount = 0
    var entityRemovedSubscribersAmount = 0


    internal fun registerSystem(system: EventSystem<*>) = update(system, added = true)

    internal fun removeSystem(system: EventSystem<*>) = update(system, added = false)


    private fun update(system: EventSystem<*>, added: Boolean) {
        val eventsTypesSubscription = system.eventsTypesSubscription
        val eventType = eventsTypesSubscription.declaredClass
        val componentIds = eventsTypesSubscription.componentIds

        if (!eventType.isSubclassOf(InternalEvent::class) || eventType == ActingEvent::class) return

        when(eventType) {
            ComponentAddedEvent::class -> {
                for (componentId in componentIds) {
                    val oldValue = componentAddedSubscribersAmount[componentId]
                    val newValue = oldValue + if (added) 1 else -1
                    componentAddedSubscribersAmount[componentId] = newValue
                    world.entityMapper.componentMappers[componentId].sendComponentAddedEvents = newValue > 0
                }
            }
            ComponentRemovedEvent::class -> {
                for (componentId in componentIds) {
                    val oldValue = componentRemovedSubscribersAmount[componentId]
                    val newValue = oldValue + if (added) 1 else -1
                    componentRemovedSubscribersAmount[componentId] = newValue
                    world.entityMapper.componentMappers[componentId].sendComponentRemovedEvents = newValue > 0
                }
            }
            ComponentChangedEvent::class -> {
                for (componentId in componentIds) {
                    val oldValue = componentChangedSubscribersAmount[componentId]
                    val newValue = oldValue + if (added) 1 else -1
                    componentChangedSubscribersAmount[componentId] = newValue
                    world.observableComponentChangeManager.sendComponentChangedEvents[componentId] = newValue > 0
                }
            }
            EntityAddedEvent::class -> {
                entityAddedSubscribersAmount += if (added) 1 else -1
                world.entityMapper.sendEntityAddedEvents = entityAddedSubscribersAmount > 0
            }
            EntityRemovedEvent::class -> {
                entityRemovedSubscribersAmount += if (added) 1 else -1
                world.entityMapper.sendEntityRemovedEvents = entityRemovedSubscribersAmount > 0
            }
        }
    }

}