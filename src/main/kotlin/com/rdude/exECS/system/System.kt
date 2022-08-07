package com.rdude.exECS.system

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World
import com.rdude.exECS.world.WorldAccessor

/** Simplest System, superclass of all systems.
 * Extending this class is well suited for creation of utility systems.
 * @see EventSystem
 * @see IterableEventSystem
 * @see ActingSystem
 * @see IterableActingSystem*/
abstract class System : WorldAccessor() {

    /** [World] in which this system is registered.*/
    @Transient
    override var world: World? = null
        internal set

    @Transient
    internal val typeId: Int = ExEcs.systemTypeIDsResolver.idFor(this::class)

}