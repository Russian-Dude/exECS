package com.rdude.exECS.component.observable.change

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.observable.ObservableFloatComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/** Represents a change of the value of [ObservableFloatComponent].*/
class FloatChange private constructor() : ComponentChange, Poolable {

    var oldValue: Float = 0f
        internal set

    var newValue: Float = 0f
        internal set

    internal companion object {
        val pool = Pool { FloatChange() }
    }
}