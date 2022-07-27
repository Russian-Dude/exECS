package com.rdude.exECS.component

import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.entity.Entity

/** Components are classes that holds some data about an [Entity][Entity] they plugged into.
 * To create your own [Component], implement this interface.
 *
 * Ideally components should not have any behaviour and be used only as data storages.
 * In exECS one Component can be shared between any number of [Entities][Entity] (except for the [UniqueComponent]).
 *
 * In exECS Component is implemented as an interface in order not to restrict its use with other classes,
 * allowing this interface to be combined with classes from other libraries and frameworks if necessary.
 * @see ImmutableComponent
 * @see UniqueComponent
 * @see RichComponent
 * @see ObservableComponent
 * @see Poolable*/
interface Component {

    /** Get ID of the component type.
     *
     * **Override this method only and only if you know what are you doing!**
     *
     * If exECS compiler plugin is enabled and this method is not overridden by user, it will be overridden by
     * [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Type-id-optimizations)
     * optimized method at compile time to improve performance.*/
    fun getComponentTypeId() = ExEcs.componentTypeIDsResolver.idFor(this::class)

}