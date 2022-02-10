package com.rdude.exECS.system

import com.rdude.exECS.aspect.Aspect
import com.rdude.exECS.aspect.EntitiesSubscription
import com.rdude.exECS.component.Component
import com.rdude.exECS.event.Event
import com.rdude.exECS.world.World
import kotlin.reflect.KClass

abstract class System {

    abstract val aspect: Aspect
    lateinit var world: World
    internal lateinit var entitiesSubscription: EntitiesSubscription

    fun createEntity(vararg components: Component) = world.createEntity(*components)

    fun queueEvent(event: Event) = world.queueEvent(event)

    override fun toString(): String {
        return "System-${this::class.simpleName}"
    }


    protected companion object {
        @JvmStatic
        protected infix fun KClass<out Component>.and(other: KClass<out Component>): MutableList<KClass<out Component>> =
            mutableListOf(this, other)

        @JvmStatic
        protected infix fun MutableList<KClass<out Component>>.and(other: KClass<out Component>): MutableList<KClass<out Component>> {
            add(other)
            return this
        }
    }
}