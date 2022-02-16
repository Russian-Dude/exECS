package com.rdude.exECS.component

import kotlin.reflect.KClass

class ComponentRetriever<T : Component> private constructor(internal val componentTypeID: ComponentTypeID) {

    companion object {

        inline operator fun <reified T : Component> invoke() = invoke(T::class)

        @Suppress("UNCHECKED_CAST")
        operator fun <T : Component> invoke(componentClass: KClass<T>): ComponentRetriever<T> {
            var retriever = instances[componentClass]
            if (retriever == null) {
                retriever = ComponentRetriever<T>(ComponentTypeIDsResolver.idFor(componentClass))
                instances[componentClass] = retriever
            }
            return retriever as ComponentRetriever<T>
        }

        private val instances: MutableMap<KClass<out Component>, ComponentRetriever<*>> = HashMap()
    }

}