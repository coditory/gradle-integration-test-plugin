package com.coditory.gradle.integration.base

import com.coditory.gradle.integration.IntegrationTestPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass

class SpecProjectBuilder private constructor(projectDir: File, name: String) {
    private val project = ProjectBuilder.builder()
        .withProjectDir(projectDir)
        .withName(name)
        .build() as DefaultProject

    fun withGroup(group: String): SpecProjectBuilder {
        project.group = group
        return this
    }

    fun withVersion(version: String): SpecProjectBuilder {
        project.version = version
        return this
    }

    fun withExtProperty(name: String, value: String): SpecProjectBuilder {
        project.extensions.extraProperties[name] = value
        return this
    }

    fun withPlugins(vararg plugins: KClass<out Plugin<*>>): SpecProjectBuilder {
        plugins
            .toList()
            .forEach { project.plugins.apply(it.java) }
        return this
    }

    fun withBuildGradle(content: String): SpecProjectBuilder {
        val buildFile = project.rootDir.resolve("build.gradle")
        buildFile.writeText(content.trimIndent().trim())
        return this
    }

    fun withFile(path: String, content: String): SpecProjectBuilder {
        val filePath = project.rootDir.resolve(path).toPath()
        Files.createDirectories(filePath.parent)
        val testFile = Files.createFile(filePath).toFile()
        testFile.writeText(content.trimIndent().trim())
        return this
    }

    fun withDirectory(path: String): SpecProjectBuilder {
        val filePath = project.rootDir.resolve(path).toPath()
        Files.createDirectories(filePath)
        return this
    }

    fun build(): Project {
        project.evaluate()
        return project
    }

    companion object {
        private var projectDirs = mutableListOf<File>()

        fun createProject(): Project {
            return projectWithPlugins().build()
        }

        fun project(name: String = "sample-project"): SpecProjectBuilder {
            return SpecProjectBuilder(createProjectDir(name), name)
        }

        fun projectWithPlugins(name: String = "sample-project"): SpecProjectBuilder {
            return project(name)
                .withPlugins(JavaPlugin::class, IntegrationTestPlugin::class)
        }

        private fun createProjectDir(projectName: String): File {
            removeProjectDirs()
            val projectParentDir = createTempDir()
            val projectDir = projectParentDir.resolve(projectName)
            projectDir.mkdir()
            projectDirs.add(projectDir)
            return projectDir
        }

        fun removeProjectDirs() {
            projectDirs.forEach {
                it.deleteRecursively()
            }
        }
    }
}
