package com.rdude.exECS.component.observable

import com.rdude.exECS.component.ObservableComponent
import com.rdude.exECS.component.observable.change.ByteChange
import com.rdude.exECS.component.value.ByteComponent
import com.rdude.exECS.event.ComponentChangedEvent

/** Observable version of [ByteComponent]. Queues [ComponentChangedEvent] every time the value changes.*/
abstract class ObservableByteComponent(value: Byte = 0) : ByteComponent(value), ObservableComponent<ByteChange> {

    final override var value: Byte
        get() = super.value
        set(value) {
            val oldValue = super.value
            super.value = value
            if (oldValue != value) {
                componentChanged(ByteChange.pool.obtain().apply {
                    this.oldValue = oldValue
                    this.newValue = value
                })
            }
        }
}