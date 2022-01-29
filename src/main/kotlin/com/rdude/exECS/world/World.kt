package com.rdude.exECS.world

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.*
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.*
import com.rdude.exECS.utils.collections.IterableList

class World {

    private val systems = IterableList<System>()
    private val eventBus = EventBus()
    private val actingEvent = ActingEvent(0.0)
    private val entityAddedEvents = Pool { EntityAddedEvent(Entity.DUMMY_ENTITY, this) }
    private val entityRemovedEvents = Pool { EntityRemovedEvent(Entity.DUMMY_ENTITY, this) }

    init {
        addSystem(ComponentAddedSystem())
        addSystem(ComponentRemovedSystem())
    }

    fun act(delta: Double) {
        actingEvent.delta = delta
        eventBus.queueEvent(actingEvent)
        eventBus.fireEvents()
    }

    fun queueEvent(event: Event) = eventBus.queueEvent(event)

    fun addEntity(entity: Entity) {
        for (system in systems) {
            if (isEntityMatchSystem(entity, system)) {
                system.addEntity(entity)
            }
        }
        val event = entityAddedEvents.obtain()
        event.entity = entity
        event.world = this
        queueEvent(event)
    }

    fun createEntity(vararg components: Component) = addEntity(Entity.new(this, *components))

    fun removeEntity(entity: Entity) {
        for (system in systems) {
            if (isEntityMatchSystem(entity, system)) {
                system.removeEntity(entity)
            }
        }
        val event = entityRemovedEvents.obtain()
        event.entity = entity
        event.world = this
        queueEvent(event)
    }

    fun addSystem(system: System) {
        checkSystemCorrectness(system)
        systems.add(system)
        if (system is EventSystem<*>) {
            eventBus.registerSystem(system)
        }
        if (system.aspect.anyOf.isEmpty() && system.aspect.anyOf.isEmpty()) {
            system.addEntity(Entity.DUMMY_ENTITY)
        }
    }

    fun removeSystem(system: System) {
        systems.remove(system)
        if (systems is EventSystem<*>) {
            eventBus.removeSystem(systems)
        }
        if (system.aspect.anyOf.isEmpty() && system.aspect.anyOf.isEmpty()) {
            system.removeEntity(Entity.DUMMY_ENTITY)
        }
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

    private fun isEntityMatchSystem(entity: Entity, system: System): Boolean {
        if (system.aspect.anyOf.isEmpty() && system.aspect.allOf.isEmpty()) {
            return false
        }
        // exclude
        for (component in system.aspect.exclude) {
            if (entity.hasComponent(component)) {
                return false
            }
        }
        // any of
        if (system.aspect.anyOf.isNotEmpty()) {
            var anyOf = false
            for (component in system.aspect.anyOf) {
                if (entity.hasComponent(component)) {
                    anyOf = true
                    system.aspect.anyOf.forceBreak()
                }
            }
            if (!anyOf) {
                return false
            }
        }
        // all of
        for (component in system.aspect.allOf) {
            if (!entity.hasComponent(component)) {
                return false
            }
        }
        return true
    }



    private inner class ComponentAddedSystem : SimpleEventSystem<ComponentAddedEvent>() {

        override fun eventFired(event: ComponentAddedEvent) {
            val entity = event.entity
            for (system in systems) {
                if (isEntityMatchSystem(entity, system) && !system.entities.contains(entity)) {
                    system.addEntity(entity)
                }
                else {
                    system.removeEntity(entity)
                }
            }
        }
    }



    private inner class ComponentRemovedSystem : SimpleEventSystem<ComponentRemovedEvent>() {

        override fun eventFired(event: ComponentRemovedEvent) {
            val entity = event.entity
            for (system in systems) {
                if (isEntityMatchSystem(entity, system) && !system.entities.contains(entity)) {
                    system.addEntity(entity)
                }
                else {
                    system.removeEntity(entity)
                }
            }
        }
    }

}