package com.rdude.exECS.component.observable

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.observable.change.IntChange
import com.rdude.exECS.component.value.IntComponent
import com.rdude.exECS.event.ComponentChangedEvent

/** Observable version of [IntComponent]. Queues [ComponentChangedEvent] every time the value changes.*/
abstract class ObservableIntComponent(value: Int = 0) : IntComponent(value), ObservableComponent<IntChange> {

    final override var value: Int
        get() = super.value
        set(value) {
            val oldValue = super.value
            super.value = value
            if (oldValue != value) {
                componentChanged(IntChange.pool.obtain().apply {
                    this.oldValue = oldValue
                    this.newValue = value
                })
            }
        }
}