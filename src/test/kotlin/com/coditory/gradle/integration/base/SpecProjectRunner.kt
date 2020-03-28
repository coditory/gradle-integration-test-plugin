package com.coditory.gradle.integration.base

import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

object SpecProjectRunner {
    fun runGradle(project: Project, arguments: List<String>, gradleVersion: String? = null): BuildResult {
        val builder = GradleRunner.create()
            .withProjectDir(project.projectDir)
            .withArguments(arguments)
            .withPluginClasspath()
            .forwardOutput()
        if (!gradleVersion.isNullOrBlank() && gradleVersion != "current") {
            builder.withGradleVersion(gradleVersion)
        }
        return builder.build()
    }
}
