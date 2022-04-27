package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectComponentDuplicateException
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.component.State
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.Event
import com.rdude.exECS.inject.SystemDelegate
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

abstract class System(val aspect: Aspect) {

    @Transient
    lateinit var world: World
        private set

    var enabled = true

    @Transient
    internal var registered = false

    @Transient
    internal lateinit var entitiesSubscription: EntitiesSubscription


    init {
        ExEcs.initializeIfNeeded()
        try {
            ExEcs.aspectCorrectnessChecker.checkAndThrowIfNotCorrect(aspect)
        }
        catch (e: AspectComponentDuplicateException) {
            throw IllegalArgumentException("Duplicate component in system aspect. System: ${this::class.qualifiedName}, component: ${e.duplicate.qualifiedName}")
        }
    }


    fun createEntity(vararg components: Component) = world.createEntity(*components)

    fun queueEvent(event: Event) = world.queueEvent(event)

    inline fun <reified T> queueEvent() where T : Event, T : Poolable = world.queueEvent<T>()

    inline fun <reified T> queueEvent(apply: T.() -> Unit) where T : Event, T : Poolable = world.queueEvent(apply)

    internal fun setWorld(world: World) {
        this.world = world
        ExEcs.generatedFieldsInitializer.initialize(this)
    }

    protected inline fun <reified T : System> inject() = SystemDelegate(T::class)

    fun getEntitySingleton(cl: KClass<out SingletonEntity>) =
        world.entityMapper.singletons[ExEcs.singletonEntityIDsResolver.getId(cl)]

    inline fun <reified T : SingletonEntity> getEntitySingleton() = getEntitySingleton(T::class)

    protected fun <T : Component> EntityWrapper.getComponent(componentClass: KClass<T>): T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] as T?

    protected fun EntityWrapper.removeComponent(componentClass: KClass<out Component>) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }

    protected fun EntityWrapper.hasComponent(componentClass: KClass<out Component>): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)

    protected fun EntityWrapper.addComponent(component: Component) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)

    protected inline fun <reified T> EntityWrapper.addComponent(): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        addComponent(component)
        return component
    }

    protected inline fun <reified T> EntityWrapper.addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
        return component
    }

    protected fun EntityWrapper.remove() = world.removeEntity(entityID)

    protected inline fun <reified T : Component> EntityWrapper.getComponent(): T? = getComponent(T::class)

    protected inline fun <reified T : Component> EntityWrapper.removeComponent() = removeComponent(T::class)

    protected inline fun <reified T : Component> EntityWrapper.hasComponent() = hasComponent(T::class)

    protected operator fun EntityWrapper.plusAssign(component: Component) = addComponent(component)

    protected operator fun EntityWrapper.minusAssign(componentClass: KClass<out Component>) =
        removeComponent(componentClass)

    protected operator fun EntityWrapper.contains(componentClass: KClass<out Component>) = hasComponent(componentClass)

    protected operator fun <T : Component> EntityWrapper.get(componentClass: KClass<T>) =
        getComponent(componentClass)
            ?: throw IllegalStateException("Entity does not have a component of type $componentClass.")

    override fun toString(): String {
        return "System-${this::class.simpleName}"
    }


    protected companion object {
        @JvmStatic
        protected infix fun KClass<out Component>.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.simpleComponents.add(this)
            entry.simpleComponents.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun State.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.stateComponents.add(this)
            entry.simpleComponents.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: State): AspectEntry {
            val entry = AspectEntry()
            entry.simpleComponents.add(this)
            entry.stateComponents.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun State.and(other: State): AspectEntry {
            val entry = AspectEntry()
            entry.stateComponents.add(this)
            entry.stateComponents.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: KClass<out Component>): AspectEntry {
            simpleComponents.add(other)
            return this
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: State): AspectEntry {
            stateComponents.add(other)
            return this
        }


    }
}