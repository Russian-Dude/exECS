package com.rdude.exECS.event

import com.rdude.exECS.component.Component

sealed interface ComponentRelatedEvent<T : Component> : Event {

    val component: T

}