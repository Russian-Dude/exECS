package com.rdude.exECS.component.observable.change

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.observable.ObservableLongComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/** Represents a change of the value of [ObservableLongComponent].*/
class LongChange private constructor() : ComponentChange, Poolable {

    var oldValue: Long = 0
        internal set

    var newValue: Long = 0
        internal set

    internal companion object {
        val pool = Pool { LongChange() }
    }
}