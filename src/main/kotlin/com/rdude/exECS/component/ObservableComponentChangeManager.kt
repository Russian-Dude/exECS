package com.rdude.exECS.component

import com.rdude.exECS.event.ComponentChangedEvent
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.collections.ComponentTypeToEntityPair
import com.rdude.exECS.utils.collections.UnsafeBitSet
import com.rdude.exECS.world.World

internal class ObservableComponentChangeManager(private val world: World) {

    private val subscriptionsManager = world.subscriptionsManager

    /** Send [ComponentChangedEvent] only for those [ObservableComponent]s whose changes are subscribed to by at least one system.*/
    @JvmField internal val sendComponentChangedEvents = UnsafeBitSet(ExEcs.componentTypeIDsResolver.size)


    fun <T : ComponentChange> componentChanged(component: ObservableComponent<T>, change: T) {

        if (sendComponentChangedEvents[component.getComponentTypeId()]) {
            world.queueEvent(ComponentChangedEvent.pool.obtain().apply {
                this.component = component
                this._change = change
            })
        }

        if (component is CanBeObservedBySystem) {
            when (component) {
                is RichComponent -> component.insideEntitiesSet.forEach {
                    val componentTypeToEntity = ComponentTypeToEntityPair(
                        componentTypeId = component.getComponentTypeId(),
                        entityID = it.id
                    )
                    subscriptionsManager.componentChanged(componentTypeToEntity)
                }
                is UniqueComponent -> {
                    val componentTypeToEntity = ComponentTypeToEntityPair(
                        componentTypeId = component.getComponentTypeId(),
                        entityID = component.entityId
                    )
                    subscriptionsManager.componentChanged(componentTypeToEntity)
                }
            }
        }

        world.internalChangeOccurred = true
    }

}