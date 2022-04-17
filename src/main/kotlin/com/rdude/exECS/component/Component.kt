package com.rdude.exECS.component

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.CountByIdArray
import com.rdude.exECS.utils.collections.IdFactory
import kotlin.reflect.KClass

/** Components are classes that holds some data about an Entity they plugged into.
 * To create your own component, implement this interface.
 *
 * Ideally components should not have any behaviour and be used only as data storages.
 * In exECS one Component can be shared between any number of Entities.
 *
 * Implement [Poolable] to make your Component poolable.
 * Add poolable Component to an Entity directly from [Pool] using [EntityWrapper.addComponent].
 * When poolable Component is removed from an Entity and there are no other Entities containing this Component,
 * it will be returned to a pool.
 *
 * In exECS Component is implemented as an interface in order not to restrict its use with other classes,
 * allowing this interface to be combined with classes from other libraries and frameworks if necessary.*/
interface Component {

    /** ID of the component instance.
     *
     * Override this property only and only if you know what are you doing!
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by generated property at compile time to improve performance.*/
    val componentId: Int get() = getIdFromMap(this)

    /** Amount of Entities that contains Component instance.
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by generated property at compile time.*/
    var insideEntities: Int
        get() = getCounterForType(this::class)[componentId]
        set(value) = getCounterForType(this::class).set(componentId, value)

    /** Get ID of the component type.
     *
     * Override this method only and only if you know what are you doing!
     * If exECS compiler plugin is enabled and this method is not overridden by user, it will be overridden
     * by generated optimized method at compile time to improve performance.*/
    fun getComponentTypeId() = ExEcs.componentTypeIDsResolver.idFor(this::class)

    companion object {

        /** This map is used to store component IDs factories only for those Component subclasses that were compiled without exECS plugin.*/
        private val idFactories = mutableMapOf<KClass<out Component>, IdFactory>()

        /** This map is used to store component IDs only for those Component subclasses that were compiled without exECS plugin.*/
        private val componentIds = mutableMapOf<Component, Int>()

        /** These counters are used to store amount of Entities that contains Component instance
         * only for those Component subclasses that were compiled without exECS plugin.*/
        private val componentsInsideEntities = mutableMapOf<KClass<out Component>, CountByIdArray>()

        private fun getCounterForType(kClass: KClass<out Component>) =
            componentsInsideEntities.getOrPut(kClass) { CountByIdArray() }

        private fun <T : Component> getIdFromMap(component: T) =
            componentIds.getOrPut(component) { getIdFactoryFor(component::class).obtain() }

        private fun getIdFactoryFor(kClass: KClass<out Component>) =
            idFactories.getOrPut(kClass) { IdFactory() }

    }

}