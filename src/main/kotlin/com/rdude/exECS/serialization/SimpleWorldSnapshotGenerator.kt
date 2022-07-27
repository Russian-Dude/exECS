package com.rdude.exECS.serialization

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World

/** Generator of [SimpleWorldSnapshot].*/
object SimpleWorldSnapshotGenerator : WorldSnapshotGenerator<SimpleWorldSnapshot> {


    override fun generate(world: World): SimpleWorldSnapshot {

        val componentMappers = world.entityMapper.componentMappers
            .mapIndexed { index, componentMapper ->
                ComponentMapperSnapshot.fromArray(
                    componentMapper.backingArray,
                    ExEcs.componentTypeIDsResolver.typeById(index)
                )
            }

        val singletonEntities = world.entityMapper.singletons
            .filterNotNull()
            .map { singletonEntity ->
                SingletonSnapshot(singletonEntity.entityID, singletonEntity)
            }

        return SimpleWorldSnapshot(
            simpleEntityStartIndex = ExEcs.singletonEntityIDsResolver.size,
            simpleEntitiesAmount = world.entityMapper.size - world.entityMapper.singletons.count { it != null },
            componentMappers = componentMappers,
            singletonEntities = singletonEntities
        )
    }


    override fun snapshotToWorld(snapshot: SimpleWorldSnapshot, world: World) {

        world.clearEntities()

        val snapshotSimpleEntityStartIndex = snapshot.simpleEntityStartIndex
        val resultSimpleEntityStartIndex = ExEcs.singletonEntityIDsResolver.size
        val simpleEntityStartIndexOffset = resultSimpleEntityStartIndex - snapshotSimpleEntityStartIndex

        val actualSingletonEntitiesIds =
            IntArray(resultSimpleEntityStartIndex) // index - snapshot id; value - actual id
        val singletonEntities = snapshot.singletonEntities
        singletonEntities.forEach { singletonSnapshot ->
            actualSingletonEntitiesIds[singletonSnapshot.singletonEntity.entityID] = singletonSnapshot.id
        }

        val newSize = snapshot.simpleEntitiesAmount + resultSimpleEntityStartIndex
        world.entityMapper.growSizeTo(newSize)

        snapshot.componentMappers.forEach { componentMapperSnapshot ->
            val componentMapper = world.entityMapper.componentMappers[componentMapperSnapshot.type.componentTypeId]
            val sendComponentAddedEvents = componentMapper.sendComponentAddedEvents
            componentMapper.sendComponentAddedEvents = false
            componentMapperSnapshot.toArray().forEachIndexed { index, component ->
                if (component != null) {
                    val id =
                        if (index < resultSimpleEntityStartIndex) actualSingletonEntitiesIds[index]
                        else index + simpleEntityStartIndexOffset
                    componentMapper.unsafeSet(id, component)
                }
            }
            componentMapper.sendComponentAddedEvents = sendComponentAddedEvents
        }

        val sendEntityAddedEvents = world.entityMapper.sendEntityAddedEvents
        world.entityMapper.sendEntityAddedEvents = false

        singletonEntities.forEach { singletonSnapshot ->
            world.addSingletonEntity(singletonSnapshot.singletonEntity)
        }

        for (i in 0 until snapshot.simpleEntitiesAmount) {
            world.entityMapper.create()
        }

        world.entityMapper.sendEntityAddedEvents = sendEntityAddedEvents
    }
}