package com.rdude.exECS.world

import com.rdude.exECS.component.Component
import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.event.*
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.utils.componentTypeId
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class WorldInitializer {

    internal val autoReturnPoolableComponents = UnsafeBitSet(ExEcs.componentTypeIDsResolver.size)

    /** Expected maximum amount of [Entities][Entity].
     *
     * This property represents the initial size of internal arrays and collections to avoid unnecessary Array.copy()
     * method calls. Default capacity is 256.*/
    var entityCapacity: Int = 256


    /** If true, [Events][Event] implementing [Poolable] will be automatically returned to the [Pool] as soon
     * as they are fired.*/
    var autoReturnPoolableEventsToPool: Boolean =
        ExEcsGlobalConfiguration.worldDefaultConfiguration.autoReturnPoolableEventsToPool


    /** [EventPriority] of [ActingEvent].*/
    var actingEventPriority: EventPriority = ExEcsGlobalConfiguration.worldDefaultConfiguration.actingEventPriority


    /** If true, [ComponentAddedEvent] will be queued for every [Component] every time an [Entity] is entered the [World].*/
    var queueComponentAddedWhenEntityAdded: Boolean =
        ExEcsGlobalConfiguration.worldDefaultConfiguration.queueComponentAddedWhenEntityAdded


    /** If true, [ComponentRemovedEvent] will be queued for every [Component] every time an [Entity] is removed from the [World].*/
    var queueComponentRemovedWhenEntityRemoved: Boolean =
        ExEcsGlobalConfiguration.worldDefaultConfiguration.queueComponentRemovedWhenEntityRemoved


    /** Set if *all* [Poolable] [Components][Component] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    @JvmName("setAutoReturnAllPoolableComponentsToPool")
    fun setAutoReturnPoolableComponentsToPool(value: Boolean) {
        for (i in 0 until ExEcs.componentTypeIDsResolver.size) {
            val isPoolable = ExEcs.componentTypeIDsResolver.typeById(i).isSubclassOf(Poolable::class)
            if (!isPoolable) continue
            autoReturnPoolableComponents[i] = value
        }
    }


    /** Set if [Components][Component] of type [T] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    inline fun <reified T> setAutoReturnPoolableComponentsToPool(value: Boolean) where T : Component, T : Poolable =
        setAutoReturnPoolableComponentsToPool(T::class, value)


    /** If true, [Components][Component] of type [T] will be automatically returned to the [Pool] as soon
     * as they are not plugged into any [Entity].*/
    fun <T> setAutoReturnPoolableComponentsToPool(componentCl: KClass<T>, value: Boolean) where T : Component, T : Poolable {
        autoReturnPoolableComponents[componentCl.componentTypeId] = value
    }
}