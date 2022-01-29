package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity

class ComponentAddedEvent(var component: Component, var entity: Entity): PoolableEvent()