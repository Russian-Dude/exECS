package com.rdude.exECS.component.observable.change

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.observable.ObservableIntComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/** Represents a change of the value of [ObservableIntComponent].*/
class IntChange private constructor() : ComponentChange, Poolable {

    var oldValue: Int = 0
        internal set

    var newValue: Int = 0
        internal set

    internal companion object {
        val pool = Pool { IntChange() }
    }
}