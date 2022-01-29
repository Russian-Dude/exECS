package com.rdude.exECS.pool

interface Poolable {

    var pool: Pool<Poolable>

}