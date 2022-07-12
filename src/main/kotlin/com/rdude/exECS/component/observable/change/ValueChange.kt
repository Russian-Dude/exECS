package com.rdude.exECS.component.observable.change

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.observable.ObservableValueComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/** Represents a change of the value of [ObservableValueComponent].*/
class ValueChange<T> private constructor() : ComponentChange, Poolable {

    private var _oldValue: Any? = null
    private var _newValue: Any? = null

    var oldValue: T
        internal inline set(value) { _oldValue = value }
        get() = _oldValue as T

    var newValue: T
        internal inline set(value) { _newValue = value }
        get() = _newValue as T

    internal companion object {
        val pool = Pool { ValueChange<Any?>() }
    }
}