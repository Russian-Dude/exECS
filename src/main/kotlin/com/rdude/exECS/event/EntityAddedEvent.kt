package com.rdude.exECS.event

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.Dummies
import com.rdude.exECS.world.World

/** Every time an [Entity] is added to the [World], if at least one [System] is subscribed to this Event, this Event is queued.*/
class EntityAddedEvent internal constructor() : InternalPoolableEvent() {

    /** The [Entity] that was added to the [World].*/
    var entity: Entity = Dummies.DUMMY_ENTITY_WRAPPER
        internal set

    /** The [entity] as a [SingletonEntity] or null if it is not [SingletonEntity].*/
    var entityAsSingleton: SingletonEntity? = null
        internal set

    // hardcoded for performance
    override fun getEventTypeId(): Int = 4


    internal companion object {
        val pool = Pool { EntityAddedEvent() }
    }

}