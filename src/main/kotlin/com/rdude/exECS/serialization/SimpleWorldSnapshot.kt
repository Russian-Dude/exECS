package com.rdude.exECS.serialization

/** Snapshot of Entities (included singletons) and Components.*/
data class SimpleWorldSnapshot(

    val simpleEntityStartIndex: Int,

    val simpleEntitiesAmount: Int,

    val componentMappers: List<ComponentMapperSnapshot<*>>,

    val singletonEntities: List<SingletonSnapshot>,

) : WorldSnapshot()