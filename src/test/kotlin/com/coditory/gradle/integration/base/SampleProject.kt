package com.coditory.gradle.integration.base

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass

object SampleProject {
  fun createProject(vararg plugins: KClass<out Plugin<*>>): Project {
    val project = ProjectBuilder.builder().build()
    plugins.forEach { project.plugins.apply(it.java) }
    return project
  }

  fun creteBuildGradle(projectDir: File, content: String): File {
    val buildFile = projectDir.resolve("build.gradle")
    buildFile.writeText(content.trimIndent().trim())
    return buildFile
  }

  fun createProjectFile(projectDir: File, path: String, content: String): File {
    val filePath = projectDir.resolve(path).toPath()
    Files.createDirectories(filePath.parent)
    val testFile = Files.createFile(filePath).toFile()
    testFile.writeText(content.trimIndent().trim())
    return testFile
  }
}
