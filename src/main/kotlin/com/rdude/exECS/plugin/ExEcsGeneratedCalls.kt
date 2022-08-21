package com.rdude.exECS.plugin

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.SingletonEntity
import com.rdude.exECS.event.Event
import com.rdude.exECS.system.System
import com.rdude.exECS.utils.componentTypeId
import com.rdude.exECS.utils.eventTypeId
import com.rdude.exECS.utils.singletonTypeId
import com.rdude.exECS.utils.systemTypeId

object ExEcsGeneratedCalls {

    inline fun <T : Component> getComponentFromSingletonEntityByComponentTypeId(singletonEntity: SingletonEntity, componentTypeId: Int): T? =
        singletonEntity.getComponent(componentTypeId)

    inline fun hasComponentFromSingletonEntityByComponentTypeId(singletonEntity: SingletonEntity, componentTypeId: Int): Boolean =
        singletonEntity.hasComponent(componentTypeId)

    inline fun removeComponentFromSingletonEntityByComponentTypeId(singletonEntity: SingletonEntity, componentTypeId: Int) =
        singletonEntity.removeComponent(componentTypeId)

    inline fun <reified T : Component> getComponentTypeId(): Int = T::class.componentTypeId

    inline fun <reified T : Event> getEventTypeId(): Int = T::class.eventTypeId

    inline fun <reified T : System> getSystemTypeId(): Int = T::class.systemTypeId

    inline fun <reified T : SingletonEntity> getSingletonEntityTypeId(): Int = T::class.singletonTypeId

}