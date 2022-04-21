package com.rdude.exECS.serialization

import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.world.World

object SimpleWorldSnapshotGenerator : WorldSnapshotGenerator<SimpleWorldSnapshot> {


    override fun generate(world: World): SimpleWorldSnapshot {

        world.rearrange()

        val result = SimpleWorldSnapshot()

        result.entitiesAmount = world.entityMapper.size

        result.systems = world.systems
            .groupBy { system -> system.entitiesSubscription }
            .map { entry ->
                SystemsSnapshot(entry.key.hasEntities.data.dropLastWhile { it == 0L }.toLongArray(), entry.value)
            }

        result.componentMappers = world.entityMapper.componentMappers
            .withIndex()
            .map { (index, mapper) -> ComponentMapperSnapshot.fromArray(mapper.backingArray, ExEcs.componentTypeIDsResolver.typeById(index)) }

        result.events = world.eventBus.eventQueue.asArray().filterNotNull()

        return result
    }


    override fun snapshotToWorld(snapshot: SimpleWorldSnapshot): World {

        val result = World()

        val entitiesAmount = snapshot.entitiesAmount
        val subscriptions = snapshot.systems
        val componentMappers = snapshot.componentMappers
        val events = snapshot.events

        result.entityMapper.size = entitiesAmount

        subscriptions.forEach { snap ->
            val systems = snap.systems
            val subs = snap.subscriptions
            systems.forEach { result.addSystem(it) }
            val subscription = systems[0].entitiesSubscription
            val bitsetData = subs.copyOf((entitiesAmount / 64 + 1).upperPowerOf2())
            subscription.hasEntities.data = bitsetData
            var trueValues = subscription.hasEntities.getTrueValues().toIntArray()
            trueValues = trueValues.copyOf(trueValues.size.upperPowerOf2())
            subscription.entityIDs.backingArray = trueValues
        }

        componentMappers.forEach { snap ->
            result.entityMapper.componentMappers[ExEcs.componentTypeIDsResolver.idFor(snap.type)].setBackingArrayUnsafe(snap.toArray())
        }
        result.entityMapper.componentMappersSize = entitiesAmount.upperPowerOf2()

        result.eventBus.eventQueue.setBackingArrayUnsafe(events.toTypedArray())

        return result
    }


    private fun Int.upperPowerOf2(): Int {
        var result = 1
        while (result < this) result *= 2
        return result
    }
}