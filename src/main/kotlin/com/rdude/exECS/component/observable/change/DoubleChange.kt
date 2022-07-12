package com.rdude.exECS.component.observable.change

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.observable.ObservableDoubleComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/** Represents a change of the value of [ObservableDoubleComponent].*/
class DoubleChange private constructor() : ComponentChange, Poolable {

    var oldValue: Double = 0.0
        internal set

    var newValue: Double = 0.0
        internal set

    internal companion object {
        val pool = Pool { DoubleChange() }
    }
}