package com.rdude.exECS.pool

import com.rdude.exECS.component.Component
import com.rdude.exECS.config.WorldConfiguration
import com.rdude.exECS.config.ExEcsGlobalConfiguration
import com.rdude.exECS.config.OnPoolIsNotSet
import com.rdude.exECS.event.Event
import com.rdude.exECS.entity.Entity
import com.rdude.exECS.exception.PoolNotSetException
import com.rdude.exECS.exception.DefaultPoolNotExistException
import com.rdude.exECS.utils.ExEcs
import java.util.IdentityHashMap

/** [Poolables][Poolable] can be used to reduce Garbage Collector calls.
 *
 * [Components][Component] that implement this interface, will automatically be returned to the [Pool] when the [Component] is removed
 * from an [Entity] and there are no other [Entities][Entity] containing that [Component].
 * This behavior can be changed with [WorldConfiguration.autoReturnPoolableComponentsToPool]
 *
 * [Events][Event] that implementing this interface, will be automatically returned to the [Pool] after being fired.
 * This behavior can be changed with [WorldConfiguration.autoReturnPoolableEventsToPool]
 *
 * Default [Pool] will be generated for every non abstract [Poolable] class. To obtain [Poolable] from the default Pool
 * call [fromPool] function.
 *
 * ```
 * val myPoolable = fromPool<MyPoolable>() // obtain from the default Pool
 * val myPoolable = fromPool<MyPoolable>() { value = 717 } // obtain from the default Pool and change
 * entity.addComponent<MyComponent>() // obtain Poolable Component from the default Pool and add it to the Entity
 * entity.addComponent<MyComponent> { value = 717 } // obtain Poolable Component from the default Pool, change it and add it to the Entity
 * queueEvent<MyEvent>() // obtain Poolable Event from the default Pool and queue it
 * queueEvent<MyEvent> { value = 717 } // obtain Poolable Event from the default Pool, change it and queue it
 * ```
 * If exECS compiler plugin is enabled, [optimizations](https://github.com/Russian-Dude/execs-plugin/wiki/Poolables-optimizations) will be applied.*/
interface Poolable {

    /** [Pool] that contains this Poolable. This property is set by the [Pool] from which [Poolable] is obtained.
     *
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Poolables-optimizations) property at compile time.*/
    var pool: Pool<Poolable>?
        get() = ExEcs.defaultPools.customPools[this] ?: ExEcs.defaultPools[this::class]
        set(value) { ExEcs.defaultPools.customPools[this] = value }

    /** True if this Poolable is in [Pool], false if this Poolable is obtained.
     *
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Poolables-optimizations) property at compile time.*/
    var isInPool: Boolean
        get() = isInPoolMap[this] == true
        set(value) { isInPoolMap[this] = value }

    /** Returns this Poolable to the [pool].
     * @throws PoolNotSetException if [pool] property is null
     * and [ExEcsGlobalConfiguration.onPoolIsNotSet] is set to [OnPoolIsNotSet.THROW].
     * @throws DefaultPoolNotExistException if [pool] is null
     * and [ExEcsGlobalConfiguration.onPoolIsNotSet] is set to [OnPoolIsNotSet.RETURN_TO_DEFAULT_POOL]
     * and the default Pool for the type of this Poolable does not exist.*/
    fun returnToPool() {
        pool?.add(this)
            ?: if (ExEcsGlobalConfiguration.onPoolIsNotSet == OnPoolIsNotSet.THROW) throw PoolNotSetException(this::class)
            else if (ExEcsGlobalConfiguration.onPoolIsNotSet == OnPoolIsNotSet.RETURN_TO_DEFAULT_POOL) {
                pool = ExEcs.defaultPools.defaultPool(this::class)
                returnToPool()
            }
    }

    /** This method is called every time this Poolable is returned to [Pool].
     * Default implementation do nothing.*/
    fun reset() {}

    companion object {

        /** This map is used to store [isInPool] values only for those Poolable subclasses that were compiled without exECS plugin.*/
        internal val isInPoolMap = IdentityHashMap<Poolable, Boolean>()

    }

}