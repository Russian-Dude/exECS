package com.rdude.exECS.event

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.system.System
import com.rdude.exECS.system.EventSystem
import com.rdude.exECS.system.SimpleEventSystem
import com.rdude.exECS.world.World
import com.rdude.exECS.pool.Poolable
import com.rdude.exECS.pool.Pool

/** Events are preferred way to communicate between [Systems][System]. To create your own Event implement this interface.
 *
 * To queue an Event call [System.queueEvent] or [World.queueEvent].
 *
 * [Poolable] Events will be returned to the [Pool] automatically after they are processed by the Event Bus.
 *
 * @see EventSystem
 * @see SimpleEventSystem
 * @see Poolable
 * @see ComponentAddedEvent
 * @see ComponentRemovedEvent
 * @see ComponentChangedEvent
 * @see EntityAddedEvent
 * @see EntityRemovedEvent*/
interface Event {

    /** Get ID of the event type.
     *
     * **Override this method only and only if you know what are you doing!**
     *
     * If exECS compiler plugin is enabled and this method is not overridden by user, it will be overridden by
     * [generated](https://github.com/Russian-Dude/execs-plugin/wiki/Type-id-optimizations)
     * optimized method at compile time to improve performance.*/
    fun getEventTypeId(): Int = ExEcs.eventTypeIDsResolver.idFor(this::class)

}