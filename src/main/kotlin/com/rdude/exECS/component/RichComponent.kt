package com.rdude.exECS.component

import com.rdude.exECS.entity.EntityWrapper
import java.util.*


/** Rich component is a [Component] that knows to which entity it plugged into.
 * Unlike simple [Component], it can only be plugged into one entity. */
interface RichComponent : Component {

    /** ID of an entity this component is plugged into. -1 if there is no entity.
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by generated property at compile time. */
    var entityId: Int
        get() = componentsToEntityId.getOrPut(this) { -1 }
        set(value) { componentsToEntityId[this] = value }

    /** Is this component is plugged into any entity. */
    val isInsideEntity: Boolean get() = entityId >= 0

    /** Get [EntityWrapper] of an entity this component is plugged into, or throw if there is no entity. */
    fun getEntity() =
        if (isInsideEntity) EntityWrapper(entityId) else throw IllegalStateException("Component is not plugged into an entity")


    companion object {

        /** This map is used to store in which entity rich component is currently plugged into
         * only for those RichComponent subclasses that were compiled without exECS plugin.*/
        internal val componentsToEntityId: MutableMap<RichComponent, Int> = IdentityHashMap()
    }

}