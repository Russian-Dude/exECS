package com.rdude.exECS.event

import com.rdude.exECS.entity.Entity
import com.rdude.exECS.world.World

class EntityRemovedEvent(var entity: Entity, var world: World): PoolableEvent()