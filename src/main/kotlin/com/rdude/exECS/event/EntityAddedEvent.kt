package com.rdude.exECS.event

import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.utils.Dummies

class EntityAddedEvent internal constructor() : InternalPoolableEvent() {

    // hardcoded for performance
    override fun getTypeId(): Int = 3

    var entity: EntityWrapper = Dummies.DUMMY_ENTITY_WRAPPER
        internal set

}