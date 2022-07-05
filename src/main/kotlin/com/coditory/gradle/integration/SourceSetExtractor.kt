package com.coditory.gradle.integration

import com.coditory.gradle.integration.shared.SemVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer

object SourceSetExtractor {
    private val GRADLE_VERSION_WITH_EXTENSION = SemVersion.parse("7.1.0")

    @Suppress("DEPRECATION")
    fun sourceSets(project: Project): SourceSetContainer {
        return if (usesExtension(project)) {
            project.extensions.getByType(JavaPluginExtension::class.java)
                .sourceSets
        } else {
            project.convention.getPlugin(org.gradle.api.plugins.JavaPluginConvention::class.java)
                .sourceSets
        }
    }

    private fun usesExtension(project: Project): Boolean {
        val gradleVersion = SemVersion.parseOrNull(project.gradle.gradleVersion)
            ?: GRADLE_VERSION_WITH_EXTENSION
        return gradleVersion >= GRADLE_VERSION_WITH_EXTENSION
    }
}
