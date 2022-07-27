package com.rdude.exECS.component.state

import com.rdude.exECS.component.*
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.collections.IterableArray
import com.rdude.exECS.world.World
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


internal fun componentStateManagerForType(type: KClass<out Component>, world: World): ComponentStateManager<*>? {

    val managers = IterableArray<ComponentStateManager<*>>()

    // Observable
    if (type.isSubclassOf(ObservableComponent::class)) {
        managers.add(ObservableComponentStateManager(world))
    }

    // PoolableComponent
    if (type.isSubclassOf(PoolableComponent::class)) {
        managers.add(PoolableComponentStateManager(world))
    }

    // FakePoolable
    if (type.isSubclassOf(Poolable::class) && !type.isSubclassOf(PoolableComponent::class)) {
        managers.add(FakePoolableComponentStateManager(world))
    }

    // RichComponent
    if (type.isSubclassOf(RichComponent::class)) {
        managers.add(RichComponentStateManager(world))
    }

    // UniqueComponent
    if (type.isSubclassOf(UniqueComponent::class)) {
        managers.add(UniqueComponentStateManager(world))
    }

    return if (managers.size > 1) ComplexComponentStateManager(world, managers)
    else if (managers.size == 1) managers[0]
    else null
}