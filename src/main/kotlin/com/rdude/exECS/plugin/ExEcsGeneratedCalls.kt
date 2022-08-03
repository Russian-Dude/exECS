package com.rdude.exECS.plugin

import com.rdude.exECS.component.Component
import com.rdude.exECS.entity.SingletonEntity

object ExEcsGeneratedCalls {

    inline fun <T : Component> getComponentFromSingletonEntityByComponentTypeId(singletonEntity: SingletonEntity, componentTypeId: Int): T? =
        singletonEntity.getComponent(componentTypeId)

    inline fun hasComponentFromSingletonEntityByComponentTypeId(singletonEntity: SingletonEntity, componentTypeId: Int): Boolean =
        singletonEntity.hasComponent(componentTypeId)

}