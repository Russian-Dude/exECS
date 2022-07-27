package com.rdude.exECS.system

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor

abstract class System : WorldAccessor() {

    init {
        ExEcs.initializeIfNeeded()
    }

    @Transient
    override var world: World? = null
        internal set

    @Transient
    internal val typeId: Int = ExEcs.systemTypeIDsResolver.idFor(this::class)

}