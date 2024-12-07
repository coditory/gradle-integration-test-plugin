package com.coditory.gradle.integration.base

import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class TestProject(private val project: Project) : Project by project {
    fun toBuildPath(vararg paths: String): String {
        return paths.joinToString(File.pathSeparator) {
            "${this.layout.buildDirectory.get()}${File.separator}${it.replace("/", File.separator)}"
        }
    }

    fun readFileFromBuildDir(path: String): String {
        return this.layout.buildDirectory.file(path).get().asFile.readText()
    }

    fun runGradle(arguments: List<String>, gradleVersion: String? = null): BuildResult {
        return gradleRunner(this, arguments, gradleVersion).build()
    }

    fun runGradleAndFail(arguments: List<String>, gradleVersion: String? = null): BuildResult {
        return gradleRunner(this, arguments, gradleVersion).buildAndFail()
    }

    // Used by @AutoClose test annotation
    fun close() {
        this.projectDir.deleteRecursively()
    }

    fun clean() {
        this.runGradle(listOf("clean"))
    }

    private fun gradleRunner(project: Project, args: List<String>, gradleVersion: String? = null): GradleRunner {
        val builder = GradleRunner.create()
            .withProjectDir(project.projectDir)
            .withArguments(args)
            .withPluginClasspath()
            .forwardOutput()
        if (!gradleVersion.isNullOrBlank() && gradleVersion != "current") {
            builder.withGradleVersion(gradleVersion)
        }
        return builder
    }
}
