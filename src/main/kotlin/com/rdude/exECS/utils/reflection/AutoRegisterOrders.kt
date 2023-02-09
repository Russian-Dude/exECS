package com.rdude.exECS.utils.reflection

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


internal object AutoRegisterOrders {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> byAnnotations(list: List<KClass<out T>>): List<KClass<out T>> {

        val relations: Map<KClass<*>, ByAnnotationsEntry> =
            list.associateWith { cl ->
                val before = cl.findAnnotation<Before>()
                    ?.classes
                    ?.toMutableSet()
                    ?: mutableSetOf()
                val after = cl.findAnnotation<After>()
                    ?.classes
                    ?.toMutableSet()
                    ?: mutableSetOf()
                ByAnnotationsEntry(before, after)
            }

        relations.forEach { (cl, entry) ->
            entry.after.forEach { afterCl ->
                relations[afterCl]?.before?.add(cl)
            }
            entry.before.forEach { beforeCl ->
                relations[beforeCl]?.after?.add(cl)
            }
        }

        val result = mutableListOf<KClass<*>>()

        list.forEach {
            putOrdered(it, result, relations)
        }

        return result as List<KClass<out T>>
    }


    private fun putOrdered(
        cl: KClass<*>,
        result: MutableList<KClass<*>>,
        relations: Map<KClass<*>, ByAnnotationsEntry>,
        alreadyIterated: MutableSet<KClass<*>> = mutableSetOf()
    ) {
        if (result.contains(cl)) return
        if (alreadyIterated.contains(cl)) throw IllegalStateException(recursionExceptionMessage(alreadyIterated))
        alreadyIterated += cl
        relations[cl]!!.after.forEach {
            putOrdered(it, result, relations, alreadyIterated)
        }
        result += cl
    }


    private class ByAnnotationsEntry(
        val before: MutableSet<KClass<*>> = mutableSetOf(), val after: MutableSet<KClass<*>> = mutableSetOf()
    )

    private fun recursionExceptionMessage(classes: Collection<KClass<*>>): String {
        val mainMessage = "Recursion has been detected while auto-registering elements using @Before/@After annotations. Recursion in: $classes\r\n"
        val detailedMessage = classes
            .mapNotNull { cl ->
                val before = cl.findAnnotation<Before>()?.classes
                val after = cl.findAnnotation<After>()?.classes
                if (before != null || after != null) {
                    var res = "${cl.simpleName}:"
                    if (before != null) {
                        res += " Before {"
                        res += before.joinToString(", ") { it.simpleName!! }
                        res += "}"
                    }
                    if (after != null) {
                        res += " After {"
                        res += after.joinToString(", ") { it.simpleName!! }
                        res += "}"
                    }
                    res
                }
                else null
            }
            .joinToString("\r\n")
        return mainMessage + detailedMessage
    }


}