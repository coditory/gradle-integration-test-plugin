package com.coditory.gradle.integration.shared

data class SemVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<SemVersion> {
    override operator fun compareTo(other: SemVersion): Int {
        return comparator.compare(this, other)
    }

    companion object {
        private val comparator = Comparator
            .comparingInt<SemVersion> { it.major }
            .thenComparingInt { it.minor }
            .thenComparingInt { it.patch }

        fun parse(version: String): SemVersion {
            return parseOrNull(version)
                ?: throw IllegalArgumentException("Expected semantic version. Got\"$version\"")
        }

        fun parseOrNull(version: String?): SemVersion? {
            if (version.isNullOrBlank()) {
                return null
            }
            val chunkWithNumbers = version
                .removePrefix("v")
                .split("-")
                .first()
            val numbers = chunkWithNumbers.split(".")
                .toTypedArray()
                .map { Integer.parseInt(it) }
            if (numbers.isEmpty()) {
                return null
            }
            val major = numbers.getOrNull(0) ?: 0
            val minor = numbers.getOrNull(1) ?: 0
            val patch = numbers.getOrNull(2) ?: 0
            return SemVersion(major, minor, patch)
        }
    }
}
