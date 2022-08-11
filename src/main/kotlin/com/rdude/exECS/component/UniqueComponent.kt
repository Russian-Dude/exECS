package com.rdude.exECS.component

import com.rdude.exECS.entity.Entity
import java.util.*


/** Unique component is a [Component] that knows to which entity it is plugged into.
 * Unlike any other [Component], it can only be plugged into one entity.
 *
 * Component should not implement both interfaces: [RichComponent] and [UniqueComponent].*/
interface UniqueComponent : Component, CanBeObservedBySystem {

    /** ID of an [Entity] this component is plugged into. -1 if there is no entity.
     *
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Simple-properties-overriding) property at compile time. */
    var entityId: Int
        get() = componentsToEntityId.getOrPut(this) { -1 }
        set(value) { componentsToEntityId[this] = value }

    /** Is this component is plugged into any [Entity]. */
    val isInsideEntity: Boolean get() = entityId >= 0

    /** Get [Entity] of an entity this component is plugged into, or throw if there is no entity. */
    fun getEntityOrThrow() =
        if (isInsideEntity) Entity(entityId) else throw IllegalStateException("Component is not plugged into an entity")

    /** Get [Entity] of an entity this component is plugged into, or [Entity.NO_ENTITY] if there is no entity. */
    fun getEntity() = Entity(entityId)


    companion object {

        /** This map is used to store in which entity unique component is currently plugged into
         * only for those UniqueComponent subclasses that were compiled without exECS plugin.*/
        internal val componentsToEntityId: MutableMap<UniqueComponent, Int> = IdentityHashMap()
    }

}