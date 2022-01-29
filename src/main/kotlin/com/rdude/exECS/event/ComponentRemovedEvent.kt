package com.rdude.exECS.event

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.Entity

class ComponentRemovedEvent(var component: Component, var entity: Entity): PoolableEvent()