package com.rdude.exECS.serialization

import com.rdude.exECS.event.Event


class SimpleWorldSnapshot : WorldSnapshot() {

    var entitiesAmount: Int = 0

    lateinit var systems: List<SystemsSnapshot>

    lateinit var componentMappers: List<ComponentMapperSnapshot<*>>

    lateinit var events: List<Event>

}