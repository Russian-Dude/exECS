package com.rdude.exECS.entity

/** Describes custom configuration of [EntityBlueprint].*/
abstract class EntityBlueprintConfiguration {

    @PublishedApi
    internal companion object NoConfiguration : EntityBlueprintConfiguration() {
        @JvmField internal val noConfigurationProducer: () -> NoConfiguration = { NoConfiguration }
    }
}