package com.rdude.exECS.aspect

import com.rdude.exECS.component.Component
import kotlin.reflect.KClass

internal class AspectCorrectnessChecker {

    fun checkAndThrowIfNotCorrect(aspect: Aspect) {
        checkForDuplicatesInEntry(aspect.anyOf)
        checkForDuplicatesInEntry(aspect.allOf)
        checkForDuplicatesInEntry(aspect.exclude)
        checkForDuplicatesBetweenEntries(aspect)
    }

    private fun checkForDuplicatesInEntry(aspectEntry: AspectEntry) {
        for (stateComponent in aspectEntry.stateComponents) {
            if (aspectEntry.simpleComponents.contains(stateComponent::class))
                throwDuplicateFound(stateComponent::class)
        }
        aspectEntry.stateComponents.reduceOrNull { acc, state -> if (acc == state) throwDuplicateFound(acc::class); acc }
        aspectEntry.simpleComponents.reduceOrNull { acc, state -> if (acc == state) throwDuplicateFound(acc); acc }
    }

    private fun checkForDuplicatesBetweenEntries(aspect: Aspect) {
        val anyOf = aspect.anyOf.simpleComponents + aspect.anyOf.stateComponents.map { it::class }
        val allOf = aspect.allOf.simpleComponents + aspect.allOf.stateComponents.map { it::class }
        val exclude = aspect.exclude.simpleComponents + aspect.exclude.stateComponents.map { it::class }
        val all = anyOf + allOf + exclude
        all.groupingBy { it }.eachCount().filterValues { it > 1 }.forEach { (type, _) -> throwDuplicateFound(type) }
    }

    private fun throwDuplicateFound(type: KClass<out Component>) {
        throw AspectComponentDuplicateException(type)
    }

}