package com.rdude.exECS.serialization

import com.rdude.exECS.event.Event


data class SimpleWorldSnapshot(

    val simpleEntitiesAmount: Int,

    val systems: List<SystemsSnapshot>,

    val componentMappers: List<ComponentMapperSnapshot<*>>,

    val singletonEntities: List<SingletonSnapshot>,

    val events: List<Event>

) : WorldSnapshot() {

    fun toWorld() = SimpleWorldSnapshotGenerator.snapshotToWorld(this)

}