package com.rdude.exECS.serialization

import com.rdude.exECS.component.ChildEntityComponent
import com.rdude.exECS.component.ParentEntityComponent
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.world.World

/** Generator of [SimpleWorldSnapshot].*/
object SimpleWorldSnapshotGenerator : WorldSnapshotGenerator<SimpleWorldSnapshot> {


    override fun generate(world: World): SimpleWorldSnapshot {

        val parentEntityComponentTypeId = ParentEntityComponent::class.componentTypeId
        val childEntityComponentTypeID = ChildEntityComponent::class.componentTypeId

        val componentMappers = world.entityMapper.componentMappers
            .filterNot { it.componentTypeId == parentEntityComponentTypeId || it.componentTypeId == childEntityComponentTypeID }
            .map { componentMapper ->
                ComponentMapperSnapshot.fromArray(
                    componentMapper.backingArray,
                    ExEcs.componentTypeIDsResolver.typeById(componentMapper.componentTypeId)
                )
            }

        val singletonEntities = world.entityMapper.singletons
            .filterNotNull()
            .map { singletonEntity ->
                SingletonSnapshot(singletonEntity.entityID, singletonEntity)
            }

        val parentChildRelations = EntitiesParentChildRelationsSnapshot.fromComponentMappers(world.entityMapper.parentEntityComponents)

        return SimpleWorldSnapshot(
            simpleEntityStartIndex = ExEcs.singletonEntityIDsResolver.size,
            simpleEntitiesAmount = world.entityMapper.size - world.entityMapper.singletons.count { it != null },
            componentMappers = componentMappers,
            singletonEntities = singletonEntities,
            entitiesParentChildRelationsSnapshot = parentChildRelations
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
                    componentMapper.addComponentUnsafe(id, component, false)
                }
            }
            componentMapper.sendComponentAddedEvents = sendComponentAddedEvents
        }

        var parent = -1
        var childrenLeft = 0
        for (i in snapshot.entitiesParentChildRelationsSnapshot.data) {
            if (childrenLeft == 0) {
                parent = i
                childrenLeft = -1
                continue
            }
            else if (childrenLeft == -1) {
                childrenLeft = i
                continue
            }
            world.entityMapper.addChildEntity(parent, i)
            childrenLeft--
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