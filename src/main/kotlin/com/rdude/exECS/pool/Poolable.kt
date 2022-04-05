package com.rdude.exECS.pool

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import com.rdude.exECS.event.Event
import com.rdude.exECS.component.Component

/** Use Poolables to reduce Garbage Collector calls.
 *
 * [Component]s that implement this interface, will automatically be returned to [Pool] when the Component is removed
 * from an Entity and there are no other Entities containing that Component.
 *
 * [Event]s that implementing this interface, will be automatically returned to [Pool] after being fired.*/
interface Poolable {

    /** [Pool] that contains this Poolable.*/
    var pool: Pool<Poolable>
        get() = defaultPools.getOrPut(this::class) { Pool { this::class.createInstance() } }
        set(value) {
            defaultPools[this::class] = value
        }

    /** Return this Poolable to [pool].*/
    fun returnToPool() = pool.retrieve(this)

    /** This method is called every time this Poolable is returned to [Pool].
     * Override it to change behavior. Default implementation do nothing.*/
    fun reset() {}

    companion object {

        /** This map is used to store Pools only for those Poolable subclasses that were compiled without exECS plugin.*/
        private val defaultPools: MutableMap<KClass<out Poolable>, Pool<Poolable>> = HashMap()

        /** Get default [Pool] for requested Poolable type.*/
        internal inline fun <reified T : Poolable> defaultPool() = defaultPool(T::class)

        /** Get default [Pool] for requested Poolable type.*/
        internal fun <T : Poolable> defaultPool(kClass: KClass<T>) = defaultPools.getOrPut(kClass) { Pool { kClass.createInstance() } }

    }

}