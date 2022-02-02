package com.rdude.exECS.world

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.EntityID
import com.rdude.exECS.entity.EntityMapper
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.event.*
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.*
import com.rdude.exECS.utils.collections.IterableArray

class World {

    private val systems = IterableArray<System>()
    internal val entityMapper = EntityMapper()
    internal val entityWrapper = EntityWrapper(this)
    private val eventBus = EventBus()
    private val actingEvent = ActingEvent(0.0)
    private val entityAddedEvents = Pool { EntityAddedEvent(this) }
    private val entityRemovedEvents = Pool { EntityRemovedEvent(this) }
    internal val componentAddedEventPool = Pool { ComponentAddedEvent(this) }
    internal val componentRemovedEventPool = Pool { ComponentRemovedEvent(this) }


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

    private fun addEntity(entity: Entity) : EntityID {
        val id = entityMapper.add(entity)
        for (system in systems) {
            if (isEntityMatchSystem(entity, system)) {
                system.addEntity(id)
            }
        }
        val event = entityAddedEvents.obtain()
        event.pureEntity = entity
        event.entityId = id
        queueEvent(event)
        return id
    }

    fun createEntity(vararg components: Component) = addEntity(Entity.new(*components))

    fun createEntity(components: Array<Component>) {
        val entityID: EntityID
        if (components.isNotEmpty()) {
            entityID = createEntity(components[0])
            if (components.size > 1) {
                for (i in 1 until components.size) {
                    entityMapper[entityID] += components[i]
                }
            }
        }
    }

    internal fun removeEntity(id: EntityID) {
        val entity = entityMapper[id]
        val event = entityRemovedEvents.obtain()
        event.pureEntity = entity
        event.entityId = id
        queueEvent(event)
        for (system in systems) {
            if (isEntityMatchSystem(entity, system)) {
                system.removeEntity(id)
            }
        }
        entityMapper.remove(id)
    }

    fun addSystem(system: System) {
        checkSystemCorrectness(system)
        systems.add(system)
        system.world = this
        if (system is EventSystem<*>) {
            eventBus.registerSystem(system)
        }
        if (system.aspect.anyOf.isEmpty() && system.aspect.allOf.isEmpty()) {
            system.addEntity(EntityID.DUMMY_ENTITY_ID)
        }
    }

    fun removeSystem(system: System) {
        systems.remove(system)
        if (systems is EventSystem<*>) {
            eventBus.removeSystem(systems)
        }
        if (system.aspect.anyOf.isEmpty() && system.aspect.allOf.isEmpty()) {
            system.removeEntity(EntityID.DUMMY_ENTITY_ID)
        }
    }

    fun clearEntities() {
        for (system in systems) {
            system.entityIDs.clear()
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
                    break
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
                if (isEntityMatchSystem(entity.entity, system) && !system.entityIDs.contains(entity.entityID)) {
                    system.addEntity(entity.entityID)
                }
                else {
                    system.removeEntity(entity.entityID)
                }
            }
        }
    }



    private inner class ComponentRemovedSystem : SimpleEventSystem<ComponentRemovedEvent>() {

        override fun eventFired(event: ComponentRemovedEvent) {
            val entity = event.entity
            for (system in systems) {
                if (isEntityMatchSystem(entity.entity, system) && !system.entityIDs.contains(entity.entityID)) {
                    system.addEntity(entity.entityID)
                }
                else {
                    system.removeEntity(entity.entityID)
                }
            }
        }
    }

}