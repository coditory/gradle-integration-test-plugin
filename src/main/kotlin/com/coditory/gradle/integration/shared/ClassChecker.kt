package com.coditory.gradle.integration.shared

internal object ClassChecker {
    fun isClassAvailable(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
