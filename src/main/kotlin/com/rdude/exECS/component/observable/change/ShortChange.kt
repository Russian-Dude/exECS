package com.rdude.exECS.component.observable.change

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.observable.ObservableShortComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/** Represents a change of the value of [ObservableShortComponent].*/
class ShortChange private constructor() : ComponentChange, Poolable {

    var oldValue: Short = 0
        internal set

    var newValue: Short = 0
        internal set

    internal companion object {
        val pool = Pool { ShortChange() }
    }
}