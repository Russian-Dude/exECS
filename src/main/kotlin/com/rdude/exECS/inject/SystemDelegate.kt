package com.rdude.exECS.inject

import com.rdude.exECS.system.System
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class SystemDelegate<T : System>(private val cl: KClass<T>) {

    private var system: T? = null

    operator fun getValue(thisRef: System, property: KProperty<*>): T {
        if (system == null) {
            thisRef.world.systems.forEach {
                if (it::class == cl) {
                    system = it as T
                    return@forEach
                }
            }
            if (system == null) {
                throw IllegalArgumentException("Can not inject system. World does not have system of type $cl")
            }
        }
        return system!!
    }

}