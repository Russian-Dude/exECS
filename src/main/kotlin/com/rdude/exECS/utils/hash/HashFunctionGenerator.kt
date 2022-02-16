package com.rdude.exECS.utils.hash

internal object HashFunctionGenerator {

    internal fun <T> generateFunctionArguments(components: Collection<T>): FunctionArguments {

        if (components.isEmpty()) return FunctionArguments(1, 1, false, 1)

        val defaultHashes = components.map { it.hashCode() }
        var max = defaultHashes.maxOrNull()!!
        var divider = 1
        var modulo = -1

        val dividers = listOf(
            generateDivider(defaultHashes, 2),
            generateDivider(defaultHashes, 3),
            generateDivider(defaultHashes, 5),
            generateDivider(defaultHashes, 7)
        )

        for (currentDivider in dividers) {
            val afterDivision = defaultHashes.map { it / currentDivider }
            val currentModulo = generateModulo(afterDivision)
            if (currentModulo >= 0) {
                val maxAfterMod = afterDivision.maxOf { it % (currentModulo * 1) }
                if (maxAfterMod < max) {
                    max = maxAfterMod
                    divider = currentDivider
                    modulo = currentModulo
                }
            }
            else if (afterDivision.maxOrNull()!! < max) {
                max = afterDivision.maxOrNull()!!
                divider = currentDivider
                modulo = -1
            }
        }

        return FunctionArguments(divider, modulo, modulo >= 0, max + 1)
    }

    private fun generateDivider(hashes: Collection<Int>, base: Int): Int {

        if (hashes.size == 1) return hashes.first()

        val temp = mutableListOf<Int>()

        var result = 1
        var current = base

        while (true) {
            for (hash in hashes) {
                temp.add(hash / current)
            }
            if (temp.distinct().size == hashes.size && current > 0) {
                result = current
                current *= 2
                temp.clear()
            }
            else {
                return result
            }
        }
    }

    private fun generateModulo(hashes: Collection<Int>): Int {

        val temp = mutableListOf<Int>()

        var current = hashes.size
        val max = hashes.maxOrNull()!!

        while (current < max) {
            for (hash in hashes) {
                temp.add(hash % current)
            }
            if (temp.distinct().size == hashes.size) {
                return current
            }
            else {
                temp.clear()
                current++
            }
        }
        return -1
    }

    internal class FunctionArguments(val divider: Int, val modulo: Int, val hasModulo: Boolean, val size: Int)

}