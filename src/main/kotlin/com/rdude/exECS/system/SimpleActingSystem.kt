package com.rdude.exECS.system

import com.rdude.exECS.entity.EntityWrapper

abstract class SimpleActingSystem : ActingSystem() {

    abstract fun act(delta: Double)

    final override fun act(entity: EntityWrapper, delta: Double) = act(delta)

    final override fun startActing() {}

    final override fun endActing() {}
}