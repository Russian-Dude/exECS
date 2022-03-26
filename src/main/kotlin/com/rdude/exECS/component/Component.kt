package com.rdude.exECS.component

interface Component {

    /** Will be overrided at compile time */
    fun getComponentTypeId() = ComponentTypeIDsResolver.idFor(this::class)

}