package com.rdude.exECS.component

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs

/** Components are classes that holds some data about an Entity they plugged into.
 * To create your own component, implement this interface.
 *
 * Ideally components should not have any behaviour and be used only as data storages.
 * In exECS one Component can be shared between any number of Entities (except for the [RichComponent]).
 *
 * Implement [Poolable] to make your Component poolable.
 * Add poolable Component to an Entity directly from [Pool] using [EntityWrapper.addComponent].
 * When poolable Component is removed from an Entity and there are no other Entities containing this Component,
 * it will be returned to a pool.
 *
 * In exECS Component is implemented as an interface in order not to restrict its use with other classes,
 * allowing this interface to be combined with classes from other libraries and frameworks if necessary.*/
interface Component {

    /** Get ID of the component type.
     *
     * Override this method only and only if you know what are you doing!
     * If exECS compiler plugin is enabled and this method is not overridden by user, it will be overridden
     * by generated optimized method at compile time to improve performance.*/
    fun getComponentTypeId() = ExEcs.componentTypeIDsResolver.idFor(this::class)

}