package com.rdude.exECS.component.observable

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.observable.change.FloatChange
import com.rdude.exECS.component.value.FloatComponent
import com.rdude.exECS.event.ComponentChangedEvent

/** Observable version of [FloatComponent]. Queues [ComponentChangedEvent] every time the value changes.*/
abstract class ObservableFloatComponent(value: Float = 0f) : FloatComponent(value), ObservableComponent<FloatChange> {

    final override var value: Float
        get() = super.value
        set(value) {
            val oldValue = super.value
            super.value = value
            if (oldValue != value) {
                componentChanged(FloatChange.pool.obtain().apply {
                    this.oldValue = oldValue
                    this.newValue = value
                })
            }
        }
}