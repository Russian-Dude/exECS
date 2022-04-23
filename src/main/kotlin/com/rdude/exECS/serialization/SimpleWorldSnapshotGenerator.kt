package com.rdude.exECS.serialization

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World

object SimpleWorldSnapshotGenerator : WorldSnapshotGenerator<SimpleWorldSnapshot> {


    override fun generate(world: World): SimpleWorldSnapshot {

        world.rearrange()

        val systems = world.systems
            .groupBy { system -> system.entitiesSubscription }
            .map { entry ->
                SystemsSnapshot(entry.key.hasEntities.data.dropLastWhile { it == 0L }.toLongArray(), entry.value)
            }

        val componentMappers = world.entityMapper.componentMappers
            .withIndex()
            .map { (index, mapper) ->
                val data = mapper.backingArray.drop(ExEcs.singletonEntityIDsResolver.typesAmount + 1).toTypedArray()
                ComponentMapperSnapshot.fromArray(data, ExEcs.componentTypeIDsResolver.typeById(index))
            }

        val singletonEntities = world.entityMapper.singletons
            .filterNotNull()
            .map { singletonEntity ->
                val components = world.entityMapper.componentMappers
                    .mapNotNull { mapper ->
                        mapper[singletonEntity.entityID]
                            ?.let { ExEcs.componentTypeIDsResolver.typeById(mapper.componentTypeId) to it }
                    }
                    .associate { it }
                SingletonSnapshot(components, singletonEntity)
            }

        val events = world.eventBus.eventQueue.asArray().filterNotNull()

        val simpleEntitiesAmount = world.entityMapper.size - singletonEntities.size - 1

        return SimpleWorldSnapshot(simpleEntitiesAmount, systems, componentMappers, singletonEntities, events)
    }


    override fun snapshotToWorld(snapshot: SimpleWorldSnapshot): World {

        val result = World()

        val simpleEntitiesAmount = snapshot.simpleEntitiesAmount
        val subscriptions = snapshot.systems
        val componentMappers = snapshot.componentMappers
        val singletons = snapshot.singletonEntities
        val events = snapshot.events

        result.entityMapper.size = simpleEntitiesAmount + singletons.size + 1
        result.entityMapper.nextID += simpleEntitiesAmount

        subscriptions.forEach { snap ->
            val systems = snap.systems
            val subs = snap.subscriptions
            systems.forEach { result.addSystem(it) }
            val subscription = systems[0].entitiesSubscription
            val bitsetData = subs.copyOf((result.entityMapper.size / 64 + 1).upperPowerOf2())
            subscription.hasEntities.data = bitsetData
            var trueValues = subscription.hasEntities.getTrueValues().toIntArray()
            trueValues = trueValues.copyOf(trueValues.size.upperPowerOf2())
            subscription.entityIDs.backingArray = trueValues
        }

        componentMappers.forEach { snap ->
            result.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(snap.type)].setBackingArrayUnsafe(
                snap.toArray(ExEcs.singletonEntityIDsResolver.typesAmount + 1)
            )
        }
        result.entityMapper.componentMappersSize = result.entityMapper.size.upperPowerOf2()

        singletons.forEach { singletonSnapshot ->
            val id = ExEcs.singletonEntityIDsResolver.getId(singletonSnapshot.singletonEntity::class)
            result.entityMapper.singletons[id] = singletonSnapshot.singletonEntity
            singletonSnapshot.components.forEach { (type, component) ->
                result.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(type)].unsafeSet(id, component)
            }
        }

        result.eventBus.eventQueue.setBackingArrayUnsafe(events.toTypedArray())

        return result
    }


    private fun Int.upperPowerOf2(): Int {
        var result = 1
        while (result < this) result *= 2
        return result
    }
}