package com.rdude.exECS.component.observable

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.observable.change.DoubleChange
import com.rdude.exECS.component.value.DoubleComponent
import com.rdude.exECS.event.ComponentChangedEvent

/** Observable version of [DoubleComponent]. Queues [ComponentChangedEvent] every time the value changes.*/
abstract class ObservableDoubleComponent(value: Double = 0.0) : DoubleComponent(value), ObservableComponent<DoubleChange> {

    final override var value: Double
        get() = super.value
        set(value) {
            val oldValue = super.value
            super.value = value
            if (oldValue != value) {
                componentChanged(DoubleChange.pool.obtain().apply {
                    this.oldValue = oldValue
                    this.newValue = value
                })
            }
        }
}