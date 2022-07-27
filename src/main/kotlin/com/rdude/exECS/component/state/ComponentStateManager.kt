package com.rdude.exECS.component.state

import com.rdude.exECS.component.Component
import com.rdude.exECS.component.UniqueComponent
import com.rdude.exECS.world.World

/** Manages specific changes of Components. E.g. [UniqueComponent.entityId].*/
internal abstract class ComponentStateManager<T : Component>(protected val world: World) {

    abstract fun componentAdded(component: T, entityId: Int)

    abstract fun componentRemoved(component: T, entityId: Int)

    abstract fun componentChangedId(component: T, from: Int, to: Int)

    inline fun componentAddedUnsafe(component: Component, entityId: Int) =
        componentAdded(component as T, entityId)

    inline fun componentRemovedUnsafe(component: Component, entityId: Int) =
        componentRemoved(component as T, entityId)

    inline fun componentChangedIdUnsafe(component: Component, from: Int, to: Int) =
        componentChangedId(component as T, from, to)

}