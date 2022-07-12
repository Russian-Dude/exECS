package com.rdude.exECS.system

import com.rdude.exECS.entity.Entity

abstract class SimpleActingSystem : ActingSystem() {

    abstract fun act(delta: Double)

    final override fun act(entity: Entity, delta: Double) = act(delta)

    final override fun beforeActing() {}

    final override fun afterActing() {}
}