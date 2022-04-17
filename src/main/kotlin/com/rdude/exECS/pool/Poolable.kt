package com.rdude.exECS.pool

import com.rdude.exECS.component.Component
import com.rdude.exECS.event.Event
import com.rdude.exECS.utils.ExEcs

/** Use Poolables to reduce Garbage Collector calls.
 *
 * [Component]s that implement this interface, will automatically be returned to [Pool] when the Component is removed
 * from an Entity and there are no other Entities containing that Component.
 *
 * [Event]s that implementing this interface, will be automatically returned to [Pool] after being fired.
 *
 * In ExECS Poolable is implemented as an interface in order not to restrict its use with other classes,
 * allowing this interface to be combined with classes from other libraries and frameworks if necessary.*/
interface Poolable {

    /** [Pool] that contains this Poolable.
     *
     * If exECS compiler plugin is disabled, this property can be overridden as simple lateinit var property
     * to improve performance.
     *
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by generated property at compile time.*/
    var pool: Pool<Poolable>
        get() = ExEcs.defaultPools.customPools[this] ?: ExEcs.defaultPools[this::class]
        set(value) { ExEcs.defaultPools.customPools[this] = value }

    /** Return this Poolable to [pool].*/
    fun returnToPool() = pool.retrieve(this)

    /** This method is called every time this Poolable is returned to [Pool].
     * Default implementation do nothing.*/
    fun reset() {}

}