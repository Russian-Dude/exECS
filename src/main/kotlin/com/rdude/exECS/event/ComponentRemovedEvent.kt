package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.pool.Pool
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.utils.Dummies

class ComponentRemovedEvent internal constructor() : InternalPoolableEvent() {

    // hardcoded for performance
    override fun getEventTypeId(): Int = 2

    var entity: EntityWrapper = Dummies.DUMMY_ENTITY_WRAPPER
        internal set

    lateinit var component: Component
        internal set

    internal companion object {
        val pool = Pool { ComponentRemovedEvent() }
    }
}