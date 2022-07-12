package com.rdude.exECS.component.observable

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.observable.change.ValueChange
import com.rdude.exECS.event.ComponentChangedEvent

/** Component that contains a value of type [T]. Queues [ComponentChangedEvent] every time the value changes.*/
abstract class ObservableValueComponent<T>(initialValue: T) : ObservableComponent<ValueChange<T>> {

    var value: T = initialValue
        set(value) {
            val oldValue = field
            field = value
            if (oldValue != value) {
                componentChanged((ValueChange.pool.obtain() as ValueChange<T>).apply {
                    this.oldValue = oldValue
                    this.newValue = value
                })
            }
        }
}