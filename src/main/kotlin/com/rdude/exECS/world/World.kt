package com.rdude.exECS.world

import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.aspect.SubscriptionsManager
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.component.ComponentPresenceChange
import com.rdude.exECS.component.ComponentTypeIDsResolver
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.*
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.*
import com.rdude.exECS.utils.collections.IterableArray

class World {

    internal val systems = IterableArray<System>()
    internal val entityMapper = EntityMapper(this)
    internal val entityWrapper = EntityWrapper(this)
    private val actingEvent = ActingEvent(0.0)
    private val eventBus = EventBus(actingEvent)
    internal val subscriptionsManager = SubscriptionsManager(this)
    internal val componentAddedEventPool = Pool { ComponentAddedEvent(this) }
    internal val componentRemovedEventPool = Pool { ComponentRemovedEvent(this) }

    fun act(delta: Double) {
        // update systems' entities
        entityMapper.notifySubscriptionsManager()
        // fire entity added/removed and component added/removed events
        eventBus.fireInternalEvents()
        // actualize data
        entityMapper.actualize()
        // set delta of main events
        actingEvent.delta = delta
        // fire events
        eventBus.fireEvents()
    }

    fun queueEvent(event: Event) = eventBus.queueEvent(event)

    internal fun queueInternalEvent(event: InternalEvent) = eventBus.queueInternalEvent(event)

    internal fun componentPresenceChange(change: ComponentPresenceChange) =
        subscriptionsManager.componentPresenceChange(change)

    fun createEntity(vararg components: Component) {
        if (components.isEmpty()) {
            throw IllegalArgumentException("Entity must have at least one component")
        }
        entityMapper.create(components)
    }

    internal fun removeEntity(id: Int) = entityMapper.requestRemove(id)

    fun addSystem(system: System) {
        checkSystemCorrectness(system)
        systems.add(system)
        system.setWorld(this)

        // share same entities subscriptions instances between systems with equals aspect
        var subscriptionsCopied = false
        for (otherSystem in systems) {
            if (otherSystem !== system && system.aspect == otherSystem.aspect) {
                system.entitiesSubscription = otherSystem.entitiesSubscription
                subscriptionsCopied = true
                break
            }
        }
        if (!subscriptionsCopied) {
            val subscription = EntitiesSubscription(system.aspect)
            system.entitiesSubscription = subscription
            entityMapper.registerEntitiesSubscription(system.entitiesSubscription)
            subscriptionsManager.add(subscription)
        }

        if (system is EventSystem<*>) {
            eventBus.registerSystem(system)
        }
    }

    fun clearEntities() {
        entityMapper.clear()
    }

    private fun checkSystemCorrectness(system: System) {
        if (
            system is EventSystem<*>
            && system.aspect.anyOf.isEmpty() && system.aspect.allOf.isEmpty()
            && !(system is SimpleActingSystem || system is SimpleEventSystem<*>)
        ) {
            val usedName = if (system is ActingSystem) ActingSystem::class.simpleName else EventSystem::class.simpleName
            val needName = if (system is ActingSystem) SimpleActingSystem::class.simpleName else SimpleEventSystem::class.simpleName
            throw IllegalStateException(
                    "System ${system::class} has no components in aspect. To use $usedName without components in aspect, use $needName instead of $usedName"
                )
        }
    }

}