package com.rdude.exECS.config

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.ComponentMapper
import com.rdude.exECS.component.PoolableComponent
import com.rdude.exECS.component.state.ComplexComponentStateManager
import com.rdude.exECS.component.state.FakePoolableComponentStateManager
import com.rdude.exECS.component.state.PoolableComponentStateManager
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.*
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class WorldConfiguration internal constructor(private val world: World) {


    /** If true, [Events][Event] implementing [Poolable] will be automatically returned to the [Pool] as soon
     * as they are fired.*/
    @JvmField
    var autoReturnPoolableEventsToPool: Boolean =
        ExEcsGlobalConfiguration.worldDefaultConfiguration.autoReturnPoolableEventsToPool


    /** [EventPriority] of [ActingEvent].*/
    @JvmField
    var actingEventPriority: EventPriority = ExEcsGlobalConfiguration.worldDefaultConfiguration.actingEventPriority


    /** If true, [ComponentAddedEvent] will be queued for every [Component] of added [Entity].*/
    @JvmField
    var queueComponentAddedWhenEntityAdded: Boolean =
        ExEcsGlobalConfiguration.worldDefaultConfiguration.queueComponentAddedWhenEntityAdded


    /** If true, [ComponentRemovedEvent] will be queued for every [Component] of removed [Entity].*/
    @JvmField
    var queueComponentRemovedWhenEntityRemoved: Boolean =
        ExEcsGlobalConfiguration.worldDefaultConfiguration.queueComponentRemovedWhenEntityRemoved


    /** If true, all [Poolable] [Components][Component] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    @JvmName("setAutoReturnAllPoolableComponentsToPool")
    fun setAutoReturnPoolableComponentsToPool(value: Boolean) {
        for (componentMapper in world.entityMapper.componentMappers) {
            val type = ExEcs.componentTypeIDsResolver.typeById(componentMapper.componentTypeId)
            if (type.isSubclassOf(Poolable::class)) {
                componentMapper.autoReturnToPool = value
            }
        }
    }


    /** If true, [Components][Component] of type [T] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    inline fun <reified T> setAutoReturnPoolableComponentsToPool(value: Boolean) where T : Component, T : Poolable =
        setAutoReturnPoolableComponentsToPool(T::class, value)


    /** If true, [Components][Component] of type [T] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    fun <T> setAutoReturnPoolableComponentsToPool(componentCl: KClass<T>, value: Boolean) where T : Component, T : Poolable {
        world.entityMapper.componentMappers[componentCl.componentTypeId].autoReturnToPool = value
    }


    /** If true, [Components][Component] of type [T] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    inline fun <reified T> getAutoReturnPoolableComponentsToPool() where T : Component, T : Poolable =
        getAutoReturnPoolableComponentsToPool(T::class)


    /** If true, [Components][Component] of type [T] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    fun <T> getAutoReturnPoolableComponentsToPool(componentCl: KClass<T>) where T : Component, T : Poolable =
        world.entityMapper.componentMappers[componentCl.componentTypeId].autoReturnToPool


    /** Sets all properties to be the same as the corresponding properties in [ExEcsGlobalConfiguration].*/
    fun setAllFromGlobalConfiguration() {
        for (componentMapper in world.entityMapper.componentMappers) {
            if (ExEcs.componentTypeIDsResolver.typeById(componentMapper.componentTypeId).isSubclassOf(Poolable::class))
                componentMapper.autoReturnToPool = ExEcsGlobalConfiguration.worldDefaultConfiguration.autoReturnPoolableComponentsToPool
        }
        autoReturnPoolableEventsToPool = ExEcsGlobalConfiguration.worldDefaultConfiguration.autoReturnPoolableEventsToPool
        actingEventPriority = ExEcsGlobalConfiguration.worldDefaultConfiguration.actingEventPriority
        queueComponentAddedWhenEntityAdded = ExEcsGlobalConfiguration.worldDefaultConfiguration.queueComponentAddedWhenEntityAdded
        queueComponentRemovedWhenEntityRemoved = ExEcsGlobalConfiguration.worldDefaultConfiguration.queueComponentRemovedWhenEntityRemoved
    }


    private var ComponentMapper<*>.autoReturnToPool: Boolean
        get() {
            return when(stateManager) {
                is PoolableComponentStateManager -> stateManager.autoReturnToPool
                is FakePoolableComponentStateManager -> stateManager.autoReturnToPool
                is ComplexComponentStateManager -> {
                    val componentType = ExEcs.componentTypeIDsResolver.typeById(componentTypeId)
                    if (componentType.isSubclassOf(PoolableComponent::class)) {
                        return stateManager.getManager<PoolableComponentStateManager>()!!.autoReturnToPool
                    }
                    else if (componentType.isSubclassOf(Poolable::class)) {
                        return stateManager.getManager<FakePoolableComponentStateManager>()!!.autoReturnToPool
                    }
                    else throw IllegalStateException("Can get auto-return Poolable Component to Pool from $componentType")
                }
                else -> throw IllegalStateException("Can get auto-return Poolable Component to Pool from ${ExEcs.componentTypeIDsResolver.typeById(componentTypeId)}")
            }
        }
        set(value) {
            when(stateManager) {
                is PoolableComponentStateManager -> stateManager.autoReturnToPool = value
                is FakePoolableComponentStateManager -> stateManager.autoReturnToPool = value
                is ComplexComponentStateManager -> {
                    stateManager.getManager<PoolableComponentStateManager>()?.autoReturnToPool = value
                    stateManager.getManager<FakePoolableComponentStateManager>()?.autoReturnToPool = value
                }
            }
        }

}