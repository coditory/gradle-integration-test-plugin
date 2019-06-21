package com.coditory.gradle.integration

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal class IntegrationTestSetConfigurator(
  private val project: Project) {

  fun setupTestSet(taskName: String, configPrefix: String, dirName: String = configPrefix)
    : TaskProvider<*> {
    setupConfiguration(configPrefix)
    val sourceSet = setupSourceSet(dirName)
    return setupTestTask(taskName, sourceSet)
  }

  private fun setupConfiguration(configPrefix: String) {
    val capitalizedName = configPrefix.capitalize()
    project.configurations.create("${configPrefix}Implementation") {
      it.extendsFrom(project.configurations.getByName("testImplementation"))
      it.isVisible = true
      it.isTransitive = true
      it.description = "$capitalizedName Implementation"
    }

    project.configurations.create("${configPrefix}RuntimeOnly") {
      it.extendsFrom(project.configurations.getByName("testRuntimeOnly"))
      it.isVisible = true
      it.isTransitive = true
      it.description = "$capitalizedName Runtime Only"
    }
  }

  private fun setupSourceSet(dirName: String): SourceSet {
    val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
    val main = javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    val test = javaConvention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
    return javaConvention.sourceSets.create(dirName) {
      it.java.srcDir("src/$dirName/java")
      it.resources.srcDir("src/$dirName/resources")
      it.compileClasspath += project.files(test.output, main.output)
      it.runtimeClasspath += it.output
      it.runtimeClasspath += project.files(test.output, main.output)
    }
  }

  private fun setupTestTask(taskName: String, sourceSet: SourceSet): TaskProvider<*> {
    val integrationTest = project.tasks.register(taskName, Test::class.java) {
      it.description = "Runs the $taskName tests."
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP
      it.testClassesDirs = sourceSet.output.classesDirs
      it.classpath = sourceSet.runtimeClasspath
      it.mustRunAfter(JavaPlugin.TEST_TASK_NAME)
    }
    // https://github.com/coditory/gradle-integration-test-plugin/issues/1
    // project.tasks.named(...) - breaks compatibility with gradle v4
    project.tasks
      .filter { it.name == JavaBasePlugin.CHECK_TASK_NAME }
      .forEach { it.dependsOn(integrationTest) }
    return integrationTest
  }
}
