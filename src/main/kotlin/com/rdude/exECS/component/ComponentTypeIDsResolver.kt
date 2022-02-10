package com.rdude.exECS.component

import com.rdude.exECS.utils.reflection.ReflectionUtils
import java.util.*
import kotlin.reflect.KClass

internal object ComponentTypeIDsResolver {

    private val map: Map<KClass<out Component>, ComponentTypeID>

    private val componentTypes: Array<KClass<out Component>?>

    internal val typesAmount: Int

    init {
        val allComponentClasses = ReflectionUtils.getAllComponentClasses()
        map = IdentityHashMap(allComponentClasses.size)
        componentTypes = allComponentClasses.toTypedArray()
        for (i in componentTypes.indices) {
            map[componentTypes[i]] = ComponentTypeID(i + 1) // component type id must be positive
        }
        typesAmount = componentTypes.size
    }

    fun idFor(componentClass: KClass<out Component>): ComponentTypeID = map[componentClass] as ComponentTypeID

    fun typeById(id: ComponentTypeID) = componentTypes[id.id] as KClass<out Component>

}