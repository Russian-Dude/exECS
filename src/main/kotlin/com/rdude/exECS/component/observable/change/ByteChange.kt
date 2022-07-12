package com.rdude.exECS.component.observable.change

import com.rdude.exECS.component.ComponentChange
import com.rdude.exECS.component.observable.ObservableByteComponent
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable

/** Represents a change of the value of [ObservableByteComponent].*/
class ByteChange private constructor() : ComponentChange, Poolable {

    var oldValue: Byte = 0
        internal set

    var newValue: Byte = 0
        internal set

    internal companion object {
        val pool = Pool { ByteChange() }
    }
}