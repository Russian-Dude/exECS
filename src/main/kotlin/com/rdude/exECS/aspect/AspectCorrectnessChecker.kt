package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.exception.AspectNotCorrectException
import com.rdude.exECS.utils.ExEcs
import com.rdude.exECS.utils.componentTypeId
import kotlin.reflect.KClass

internal class AspectCorrectnessChecker {

    fun checkAndThrowIfNotCorrect(aspect: Aspect) {
        checkForEmptyAspect(aspect)
        checkForDuplicatesInEntry(aspect.anyOf)
        checkForDuplicatesInEntry(aspect.allOf)
        checkForDuplicatesInEntry(aspect.exclude)
        checkForDuplicatesBetweenEntries(aspect)
    }

    /** Checks for duplicates in the same [AspectEntry].*/
    private fun checkForDuplicatesInEntry(aspectEntry: AspectEntry) {
        aspectEntry.types.reduceOrNull { acc, state -> if (acc == state) throwDuplicateFound(acc); acc }
    }

    /** Checks for duplicates in all [AspectEntry] instances in one [Aspect].*/
    private fun checkForDuplicatesBetweenEntries(aspect: Aspect) {
        // anyOf type, allOf type
        aspect.anyOf.types
            .find { type -> aspect.allOf.types.any { type == it } }
            ?.also { throwDuplicateFound(it) }

        // anyOf type, allOf condition
        aspect.anyOf.types
            .find { type -> aspect.allOf.conditions.map { it.componentTypeId }.any { type.componentTypeId == it } }
            ?.also { throwDuplicateFound(it) }

        // anyOf type, exclude type
        aspect.anyOf.types
            .find { type -> aspect.exclude.types.any { type == it } }
            ?.also { throwDuplicateFound(it) }

        // allOf type, exclude type
        aspect.allOf.types
            .find { type -> aspect.exclude.types.any { type == it } }
            ?.also { throwDuplicateFound(it) }

        // anyOf condition, exclude type
        aspect.anyOf.conditions
            .map { it.componentTypeId }
            .find { typeId -> aspect.exclude.types.any { typeId == it.componentTypeId } }
            ?.also { throwDuplicateFound(ExEcs.componentTypeIDsResolver.typeById(it)) }

        // allOf condition, exclude type
        aspect.allOf.conditions
            .map { it.componentTypeId }
            .find { typeId -> aspect.exclude.types.any { typeId == it.componentTypeId } }
            ?.also { throwDuplicateFound(ExEcs.componentTypeIDsResolver.typeById(it)) }
    }

    private fun checkForEmptyAspect(aspect: Aspect) {
        if (aspect.isEmpty()) throw AspectNotCorrectException("Aspect is empty")
    }

    private fun throwDuplicateFound(type: KClass<out Component>) {
        throw AspectNotCorrectException("Duplicate entries in Aspect have been detected. Duplicate: $type")
    }

}