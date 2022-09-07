package com.rdude.exECS.component

import com.rdude.exECS.utils.collections.EntitiesSet
import java.util.*

/** Rich component is a [Component] that knows to which entities it is plugged into.
 *
 * Component should not implement both interfaces: [RichComponent] and [UniqueComponent].*/
interface RichComponent : Component, CanBeObservedBySystem {

    /** IDs of entities this component is plugged into.
     *
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Simple-properties-overriding) property at compile time.*/
    val insideEntitiesSet: EntitiesSet
        get() = componentsToEntitiesIds.getOrPut(this) { EntitiesSet() }


    companion object {

        /** This map is used to store in which entities rich component is currently plugged into
         * only for those RichComponent subclasses that were compiled without exECS plugin.*/
        internal val componentsToEntitiesIds: MutableMap<RichComponent, EntitiesSet> = IdentityHashMap()
    }

}