package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import com.rdude.exECS.exception.AspectComponentDuplicateException
import kotlin.reflect.KClass

internal class AspectCorrectnessChecker {

    fun checkAndThrowIfNotCorrect(aspect: Aspect) {
        checkForDuplicatesInEntry(aspect.anyOf)
        checkForDuplicatesInEntry(aspect.allOf)
        checkForDuplicatesInEntry(aspect.exclude)
        checkForDuplicatesBetweenEntries(aspect)
    }

    /** Checks for duplicates in the same [AspectEntry].*/
    private fun checkForDuplicatesInEntry(aspectEntry: AspectEntry) {
        for (stateComponent in aspectEntry.states) {
            if (aspectEntry.types.contains(stateComponent::class))
                throwDuplicateFound(stateComponent::class)
        }
        aspectEntry.states.reduceOrNull { acc, state -> if (acc == state) throwDuplicateFound(acc::class); acc }
        aspectEntry.types.reduceOrNull { acc, state -> if (acc == state) throwDuplicateFound(acc); acc }
    }

    /** Checks for duplicates in all [AspectEntry] instances in one [Aspect].*/
    private fun checkForDuplicatesBetweenEntries(aspect: Aspect) {
        val anyOf = aspect.anyOf.types + aspect.anyOf.states.map { it::class }
        val allOf = aspect.allOf.types + aspect.allOf.states.map { it::class }
        val exclude = aspect.exclude.types + aspect.exclude.states.map { it::class }
        val all = anyOf + allOf + exclude
        all.groupingBy { it }.eachCount().filterValues { it > 1 }.forEach { (type, _) -> throwDuplicateFound(type) }
    }

    private fun throwDuplicateFound(type: KClass<out Component>) {
        throw AspectComponentDuplicateException("Duplicate entries passed to Aspect. Duplicate: $type")
    }

}