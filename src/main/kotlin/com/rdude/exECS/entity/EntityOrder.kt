package com.rdude.exECS.entity

import com.rdude.exECS.component.*
import com.rdude.exECS.utils.componentTypeId
import kotlin.reflect.KClass

class EntityOrder internal constructor(
    val orderDefinition: Definition,
    internal val dependsOnComponentTypes: IntArray,
    val comparator: EntityComparator
) {

    @PublishedApi
    internal constructor(
        orderDefinition: Definition,
        vararg dependsOnComponentTypes: KClass<out Component>,
        comparator: EntityComparator
    ) : this(
        orderDefinition,
        IntArray(dependsOnComponentTypes.size) { dependsOnComponentTypes[it].componentTypeId },
        comparator
    )

    sealed interface Definition {

        object NotSpecified : Definition

        class Custom internal constructor() : Definition

        data class By<T> @PublishedApi internal constructor(
            val cl: KClass<T>,
            val ascending: Boolean,
            val nullsFirst: Boolean
        ) : Definition where T : Component, T : Comparable<T>

        data class Having @PublishedApi internal constructor(
            val cl: KClass<out Component>,
            val nullsFirst: Boolean
        ) : Definition

    }


    companion object {

        val NOT_SPECIFIED = EntityOrder(orderDefinition = Definition.NotSpecified, comparator = EntityComparator.DO_NOT_COMPARE)

        /** Order [Entities][Entity] based on custom [comparator].
         * Entities will be sorted automatically when [Components][Component] of specified by [dependsOnComponentTypes]
         * types will be added or removed from any Entity System is subscribed to,
         * or when Component implementing [ObservableComponent] and either [RichComponent] or [UniqueComponent] will be changed.*/
        fun custom(vararg dependsOnComponentTypes: KClass<out Component>, comparator: EntityComparator) =
            EntityOrder(
                Definition.Custom(),
                IntArray(dependsOnComponentTypes.size) { dependsOnComponentTypes[it].componentTypeId },
                comparator
            )

    }

}