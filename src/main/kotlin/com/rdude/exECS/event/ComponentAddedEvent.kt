package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.EntityWrapper
import com.rdude.exECS.utils.Dummies

class ComponentAddedEvent internal constructor() : InternalPoolableEvent() {

    // hardcoded for performance
    override fun getEventTypeId(): Int = 1

    var entity: EntityWrapper = Dummies.DUMMY_ENTITY_WRAPPER
        internal set

    lateinit var component: Component
        internal set

    var replacedComponent: Component? = null
        internal set

    val replaced get() = replacedComponent != null

}