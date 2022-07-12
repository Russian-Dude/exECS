package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.AspectEntry
import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.*
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.Event
import com.rdude.exECS.inject.SystemDelegate
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import kotlin.reflect.KClass
import com.rdude.exECS.exception.EmptyEntityException

abstract class System(val aspect: Aspect = Aspect()) {

    @Transient
    lateinit var world: World
        private set

    /** If disabled, System is still registered in the [World] but does not act.*/
    var enabled = true

    @Transient
    internal var registered = false

    @Transient
    internal lateinit var entitiesSubscription: EntitiesSubscription


    init {
        ExEcs.initializeIfNeeded()
    }


    /** Creates an Entity with given components. At least one component must be passed to the arguments.
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntity(vararg components: Component) = world.createEntity(*components)

    /** Creates the specified amount of Entities that will share the given Components.
     * @throws [EmptyEntityException] if no components have been passed.*/
    fun createEntitiesWithSameComponents(amount: Int, vararg components: Component) =
        world.createEntitiesWithSameComponents(amount, *components)

    /** Creates the specified amount of Entities with Components  */
    fun createEntities(amount: Int, vararg components: (Int) -> Component) =
        world.createEntities(amount, *components)

    fun queueEvent(event: Event) = world.queueEvent(event)

    inline fun <reified T> queueEvent() where T : Event, T : Poolable = world.queueEvent<T>()

    inline fun <reified T> queueEvent(apply: T.() -> Unit) where T : Event, T : Poolable = world.queueEvent(apply)

    internal fun setWorld(world: World) {
        this.world = world
        ExEcs.generatedFieldsInitializer.initialize(this)
    }

    protected inline fun <reified T : System> inject() = SystemDelegate(T::class)

    fun <T : SingletonEntity> getEntitySingleton(cl: KClass<T>): T? =
        world.entityMapper.singletons[ExEcs.singletonEntityIDsResolver.getId(cl)] as T?

    inline fun <reified T : SingletonEntity> getEntitySingleton(): T? = getEntitySingleton(T::class)

    protected fun <T : Component> Entity.getComponent(componentClass: KClass<T>): T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][id] as T?

    protected fun Entity.removeComponent(componentClass: KClass<out Component>) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][id] = null
    }

    protected fun Entity.hasComponent(componentClass: KClass<out Component>): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(id)

    protected fun Entity.addComponent(component: Component) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(id, component)

    protected inline fun <reified T> Entity.addComponent(): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        addComponent(component)
        return component
    }

    protected inline fun <reified T> Entity.addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
        return component
    }

    protected fun Entity.remove() = world.requestRemoveEntity(id)

    protected inline fun <reified T : Component> Entity.getComponent(): T? = getComponent(T::class)

    protected inline operator fun <reified T : Component> Entity.invoke(): T? = getComponent(T::class)

    protected inline fun <reified T : Component> Entity.removeComponent() = removeComponent(T::class)

    protected inline fun <reified T : Component> Entity.hasComponent() = hasComponent(T::class)

    protected operator fun Entity.plusAssign(component: Component) = addComponent(component)

    protected operator fun Entity.minusAssign(componentClass: KClass<out Component>) =
        removeComponent(componentClass)

    protected operator fun Entity.contains(componentClass: KClass<out Component>) = hasComponent(componentClass)

    protected operator fun <T : Component> Entity.get(componentClass: KClass<T>) =
        getComponent(componentClass)
            ?: throw IllegalStateException("Entity does not have a component of type $componentClass.")

    override fun toString(): String {
        return "System-${this::class.simpleName}"
    }


    protected companion object {

        @JvmStatic
        protected inline operator fun <reified T> KClass<T>.invoke(noinline condition: T.() -> Boolean): ComponentCondition<T> where T : ObservableComponent<*>, T : CanBeObservedBySystem =
            SimpleComponentCondition(T::class, condition)

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.types.add(this)
            entry.types.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun State.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.states.add(this)
            entry.types.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: State): AspectEntry {
            val entry = AspectEntry()
            entry.types.add(this)
            entry.states.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun State.and(other: State): AspectEntry {
            val entry = AspectEntry()
            entry.states.add(this)
            entry.states.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: KClass<out Component>): AspectEntry {
            types.add(other)
            return this
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: State): AspectEntry {
            states.add(other)
            return this
        }

        @JvmStatic
        protected infix fun AspectEntry.and(other: ComponentCondition<*>): AspectEntry {
            conditions.add(other)
            return this
        }

        @JvmStatic
        protected infix fun KClass<out Component>.and(other: ComponentCondition<*>): AspectEntry {
            val entry = AspectEntry()
            entry.types.add(this)
            entry.conditions.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun State.and(other: ComponentCondition<*>): AspectEntry {
            val entry = AspectEntry()
            entry.states.add(this)
            entry.conditions.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: ComponentCondition<*>): AspectEntry {
            val entry = AspectEntry()
            entry.conditions.add(this)
            entry.conditions.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: State): AspectEntry {
            val entry = AspectEntry()
            entry.conditions.add(this)
            entry.states.add(other)
            return entry
        }

        @JvmStatic
        protected infix fun ComponentCondition<*>.and(other: KClass<out Component>): AspectEntry {
            val entry = AspectEntry()
            entry.conditions.add(this)
            entry.types.add(other)
            return entry
        }


    }
}