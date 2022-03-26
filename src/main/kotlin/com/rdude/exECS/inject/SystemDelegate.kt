package com.rdude.exECS.inject

import com.rdude.exECS.system.System
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class SystemDelegate<T : System>(private val cl: KClass<T>) {

    private var system: T? = null

    operator fun getValue(thisRef: System, property: KProperty<*>): T {
        if (system == null) {
            for (s in thisRef.world.systems) {
                if (s::class == cl) {
                    system = s as T
                    break
                }
            }
            if (system == null) {
                throw IllegalArgumentException("Can not inject system. World does not have system of type $cl")
            }
        }
        return system!!
    }

}