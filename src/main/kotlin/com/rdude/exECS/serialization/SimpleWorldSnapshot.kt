package com.rdude.exECS.serialization


data class SimpleWorldSnapshot(

    val simpleEntitiesAmount: Int,

    val systems: List<SystemsSnapshot>,

    val componentMappers: List<ComponentMapperSnapshot<*>>,

    val singletonEntities: List<SingletonSnapshot>,

) : WorldSnapshot() {

    fun toWorld() = SimpleWorldSnapshotGenerator.snapshotToWorld(this)

}