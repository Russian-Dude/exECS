package com.rdude.exECS.exception

import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.system.System

class AlreadyRegisteredException(instance: Any) : ExEcsException(
    "Can not register ${getClassTypeName(instance)} in the World. ${getClassTypeName(instance)} of type ${instance::class} is already registered") {


    private companion object {

        private fun getClassTypeName(instance: Any): String = when(instance) {
            is System -> "System"
            is SingletonEntity -> "SingletonEntity"
            else -> throw NotImplementedError()
        }

    }

}