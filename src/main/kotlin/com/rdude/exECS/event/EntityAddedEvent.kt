package com.rdude.exECS.event

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.utils.Dummies

class EntityAddedEvent internal constructor() : InternalPoolableEvent() {

    // hardcoded for performance
    override fun getEventTypeId(): Int = 3

    var entity: EntityWrapper = Dummies.DUMMY_ENTITY_WRAPPER
        internal set

    var entityAsSingleton: SingletonEntity? = null
        internal set

    internal companion object {
        val pool = Pool { EntityAddedEvent() }
    }

}