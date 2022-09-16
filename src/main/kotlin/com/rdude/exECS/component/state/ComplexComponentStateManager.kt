package com.rdude.exECS.component.state

import com.rdude.exECS.component.Component
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

/** Composition of other [ComponentStateManager]s.*/
internal class ComplexComponentStateManager(
    world: World,
    private val managers: IterableArray<ComponentStateManager<*>>
) : ComponentStateManager<Component>(world) {

    fun <T : ComponentStateManager<*>> getManager(cl: KClass<T>): T? =
        managers.firstOrNull { it::class == cl } as T?

    inline fun <reified T : ComponentStateManager<*>> getManager(): T? =
        getManager(T::class)

    override fun componentAdded(component: Component, entityId: Int) {
        managers.forEach { it.componentAddedUnsafe(component, entityId) }
    }

    override fun componentRemoved(component: Component, entityId: Int) {
        managers.forEach { it.componentRemovedUnsafe(component, entityId) }
    }

    override fun componentChangedId(component: Component, from: Int, to: Int) {
        managers.forEach { it.componentChangedIdUnsafe(component, from, to) }
    }
}