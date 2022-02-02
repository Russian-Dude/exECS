package com.rdude.exECS.entity

@JvmInline
value class EntityID(val id: Int) {

    companion object {
        val DUMMY_ENTITY_ID = EntityID(0)
    }

}