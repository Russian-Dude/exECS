package com.rdude.exECS.component.observable

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.observable.change.ShortChange
import com.rdude.exECS.component.value.ShortComponent
import com.rdude.exECS.event.ComponentChangedEvent

/** Observable version of [ShortComponent]. Queues [ComponentChangedEvent] every time the value changes.*/
abstract class ObservableShortComponent(value: Short = 0) : ShortComponent(value), ObservableComponent<ShortChange> {

    final override var value: Short
        get() = super.value
        set(value) {
            val oldValue = super.value
            super.value = value
            if (oldValue != value) {
                componentChanged(ShortChange.pool.obtain().apply {
                    this.oldValue = oldValue
                    this.newValue = value
                })
            }
        }
}