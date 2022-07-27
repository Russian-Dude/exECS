package com.rdude.exECS.component

import com.rdude.exECS.event.ComponentChangedEvent
import com.rdude.exECS.world.World
import java.util.*

/** Observable components are components that can notify subscribers when they changed.
 *
 * [ComponentChangedEvent] will be fired after the change occurred.
 *
 * Also, if component implements either [UniqueComponent] or [RichComponent], IterableSystems can subscribe not just to
 * component type but to component condition:
 * ```
 * class MySystem : IterableActingSystem(only = PositionComponent::class { x > 0 && y > 0 })
 * ```
 *
 * [componentChanged] should be called to notify the change. If component only needs to store one property,
 * it can be created in one line using classes from package [com.rdude.exECS.component.observable].*/
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