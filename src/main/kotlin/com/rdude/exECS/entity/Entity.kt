package com.rdude.exECS.entity

import com.rdude.exECS.event.Event
import com.rdude.exECS.system.IterableActingSystem
import com.rdude.exECS.system.System
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor

/**
 * Entity is a container for components. In exECS Entity is represented as an [id] value and this class is just a wrapper.
 *
 * Entity [id] is guaranteed to remain constant only during the execution of the [World.act] method and may change after
 * the execution is completed. Thus, Entity **MUST NOT BE STORED**, but it can be safely passed along with an [Event]
 * if the [Event] is queued during the execution of the [World.act] method (e.g. inside [IterableActingSystem.act] method).
 *
 * Entity methods are available from [WorldAccessor] context (from [System] and [SingletonEntity]) or [EntityUnoptimizedMethods].
 *
 * @see SingletonEntity*/
@JvmInline
value class Entity @PublishedApi internal constructor(val id: Int) {

    companion object {

        /** Since Entity is a value class, every time it is used as nullable it will be boxed.
         *  This const should be used instead of null to avoid boxing.*/
        val NO_ENTITY = Entity(-1)

    }

}