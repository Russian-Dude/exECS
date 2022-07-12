package com.rdude.exECS.component.observable

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.observable.change.LongChange
import com.rdude.exECS.component.value.LongComponent
import com.rdude.exECS.event.ComponentChangedEvent

/** Observable version of [LongComponent]. Queues [ComponentChangedEvent] every time the value changes.*/
abstract class ObservableLongComponent(value: Long = 0L) : LongComponent(value), ObservableComponent<LongChange> {

    final override var value: Long
        get() = super.value
        set(value) {
            val oldValue = super.value
            super.value = value
            if (oldValue != value) {
                componentChanged(LongChange.pool.obtain().apply {
                    this.oldValue = oldValue
                    this.newValue = value
                })
            }
        }
}