package com.rdude.exECS.component

import com.rdude.exECS.event.ComponentChangedEvent
import com.rdude.exECS.world.World
import com.rdude.exECS.system.IterableEventSystem
import com.rdude.exECS.component.observable.*
import java.util.*

/** Component that notifies about changes to its data.
 *
 * [ComponentChangedEvent] will be fired after the change occurred.
 *
 * If Observable Component also implements either [UniqueComponent] or [RichComponent],
 * [IterableSystems][IterableEventSystem] can subscribe to component conditions:
 * ```
 * class MySystem : IterableActingSystem(only = PositionComponent::class { x > 0 && y > 0 })
 * ```
 *
 * If Observable Component only needs to store one property, it is recommended to use one of the following classes instead:
 *  * [ObservableValueComponent]
 *  * [ObservableByteComponent]
 *  * [ObservableShortComponent]
 *  * [ObservableIntComponent]
 *  * [ObservableLongComponent]
 *  * [ObservableFloatComponent]
 *  * [ObservableDoubleComponent]
 *
 * Otherwise, [componentChanged] should be called to notify about the change.
 *
 * @see ImmutableComponent
 * @see RichComponent
 * @see UniqueComponent*/
interface ObservableComponent<T : ComponentChange> : Component {

    /** Current world of this component.
     * If exECS compiler plugin is enabled and this property is not overridden by user, it will be overridden
     * by [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Simple-properties-overriding) property at compile time.*/
    var world: World?
        get() = componentsToWorlds[this]
        set(value) { if (value == null) componentsToWorlds.remove(this) else componentsToWorlds[this] = value }

    /** This method should be called to notify a component change.*/
    fun componentChanged(change: T) {
        world?.componentChanged(this, change)
    }


    companion object {

        /** This map is used to store in which World ObservableComponent is currently exists
         * only for those ObservableComponents subclasses that were compiled without exECS plugin.*/
        internal val componentsToWorlds: MutableMap<ObservableComponent<*>, World> = IdentityHashMap()

    }

}