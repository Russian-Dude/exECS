package com.rdude.exECS.serialization

import com.rdude.exECS.world.World

interface WorldSnapshotGenerator<T : WorldSnapshot> {

    fun generate(world: World): T

    fun snapshotToWorld(snapshot: T, world: World)

    fun snapshotToNewWorld(snapshot: T): World = World().apply { snapshotToWorld(snapshot, this) }

}