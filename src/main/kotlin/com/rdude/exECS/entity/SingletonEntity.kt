package com.rdude.exECS.entity

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.ComponentAddedEvent
import com.rdude.exECS.event.ComponentRemovedEvent
import com.rdude.exECS.event.EntityRemovedEvent
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.fromPool
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import com.rdude.exECS.system.System
import kotlin.reflect.KClass

/** [Entity] that keeps its id constant, so it can be referenced safely, can have its own logic or/and extra properties
 *  (prefer to keep any logic in [System]s anyway). [World] can have only one instance of each SingletonEntity,
 *  and every instance of SingletonEntity can only be plugged into one [World] instance.*/
abstract class SingletonEntity {

    init {
        ExEcs.initializeIfNeeded()
    }

    @Transient
    val entityID: Int = ExEcs.singletonEntityIDsResolver.getId(this::class)

    @Transient
    lateinit var world: World
        private set

    internal val isWorldInitialized get() = ::world.isInitialized



    /** @return this as [Entity].*/
    fun asEntityWrapper(): Entity = Entity(entityID)


    /** Get [Component] of type [T] or null if this Entity does not have component of such type.*/
    fun <T : Component> getComponent(componentClass: KClass<T>): T? =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] as T?


    /** Get [Component] of type [T] or null if this Entity does not have component of such type.*/
    inline fun <reified T : Component> getComponent(): T? = getComponent(T::class)


    /** Get [Component] of type [T] or throw if this Entity does not have component of such type.
     * @throws IllegalArgumentException if if this Entity does not have component of such type*/
    operator fun <T : Component> get(componentClass: KClass<T>) =
        getComponent(componentClass)
            ?: throw IllegalStateException("Entity does not have a component of type $componentClass.")


    /** Get [Component] of type [T] or null if this Entity does not have component of such type.*/
    inline operator fun <reified T : Component> invoke(): T? = getComponent(T::class)


    /** Removes [Component] of the specified type from this Entity. Fires [ComponentRemovedEvent].*/
    fun removeComponent(componentClass: KClass<out Component>) {
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)][entityID] = null
    }


    /** Removes [Component] of the type [T] from this Entity. Fires [ComponentRemovedEvent].*/
    inline fun <reified T : Component> removeComponent() = removeComponent(T::class)


    /** Removes [Component] of the specified type from this Entity. Fires [ComponentRemovedEvent].*/
    operator fun minusAssign(componentClass: KClass<out Component>) =
        removeComponent(componentClass)


    /** Returns true if this Entity has a [Component] of the specified type.*/
    fun hasComponent(componentClass: KClass<out Component>): Boolean =
        world.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(componentClass)].hasComponent(entityID)


    /** Returns true if this Entity has a [Component] of the type [T].*/
    inline fun <reified T : Component> hasComponent() = hasComponent(T::class)


    /** Returns true if this Entity has a [Component] of the specified type.*/
    operator fun contains(componentClass: KClass<out Component>) = hasComponent(componentClass)


    /** Adds [component] to this Entity. Fires [ComponentAddedEvent].*/
    fun addComponent(component: Component) =
        world.entityMapper.componentMappers[component.getComponentTypeId()].unsafeSet(entityID, component)


    /** Obtains [Component] of type [T] from the default Pool and adds it to this Entity. Fires [ComponentAddedEvent].*/
    inline fun <reified T> addComponent(): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        addComponent(component)
        return component
    }


    /** Obtains [Component] of type [T] from the default Pool, apply [apply] function to this [Component] and adds it
     *  to this Entity. Fires [ComponentAddedEvent].*/
    inline fun <reified T> addComponent(apply: T.() -> Unit): T where T : Component, T : Poolable {
        val component = fromPool<T>()
        apply.invoke(component)
        addComponent(component)
        return component
    }


    /** Adds [component] to this Entity. Fires [ComponentAddedEvent].*/
    operator fun plusAssign(component: Component) = addComponent(component)


    /** Removes this Entity from the [World]. Fires [EntityRemovedEvent].*/
    fun remove() = world.requestRemoveEntity(entityID)

    internal fun setWorld(world: World) {
        this.world = world
        ExEcs.generatedFieldsInitializer.initialize(this, world)
    }

}