package com.rdude.exECS.component

import kotlin.reflect.KClass

class ComponentRetriever<T : Component> private constructor(internal val componentTypeID: ComponentTypeID) {

    companion object {

        inline operator fun <reified T : Component> invoke() = invoke(T::class)

        operator fun <T : Component> invoke(componentClass: KClass<T>) = ComponentRetriever<T>(ComponentTypeIDsResolver.idFor(componentClass))

    }

}