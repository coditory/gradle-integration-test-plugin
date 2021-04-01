package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_CONFIG_PREFIX
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST_TASK_NAME
import com.coditory.gradle.integration.TestSkippingConditions.skipIntegrationTest
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal object IntegrationTestTaskConfiguration {
    fun apply(project: Project) {
        val sourceSet = setupSourceSet(project)
        setupConfiguration(project)
        setupTestTask(project, sourceSet)
    }

    private fun setupSourceSet(project: Project): SourceSet {
        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val main = javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val test = javaConvention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
        return javaConvention.sourceSets.create(INTEGRATION_CONFIG_PREFIX) {
            it.compileClasspath += test.output + main.output + test.compileClasspath
            it.runtimeClasspath += test.output + main.output + test.runtimeClasspath
        }
    }

    private fun setupConfiguration(project: Project) {
        val capitalizedName = INTEGRATION_CONFIG_PREFIX.capitalize()
        project.configurations.getByName("${INTEGRATION_CONFIG_PREFIX}Implementation") {
            it.extendsFrom(project.configurations.getByName("testImplementation"))
            it.isVisible = true
            it.isTransitive = true
            it.description = "$capitalizedName Implementation"
        }
        if (project.configurations.names.contains("testRuntimeOnly")) {
            // gradle 7
            project.configurations.getByName("${INTEGRATION_CONFIG_PREFIX}RuntimeOnly") {
                it.extendsFrom(project.configurations.getByName("testRuntimeOnly"))
                it.isVisible = true
                it.isTransitive = true
                it.description = "$capitalizedName Runtime Only"
            }
        } else {
            // gradle <7
            project.configurations.getByName("${INTEGRATION_CONFIG_PREFIX}Runtime") {
                it.extendsFrom(project.configurations.getByName("testRuntime"))
                it.isVisible = true
                it.isTransitive = true
                it.description = "$capitalizedName Runtime"
            }
        }
    }

    private fun setupTestTask(project: Project, sourceSet: SourceSet) {
        val integrationTest = project.tasks.register(INTEGRATION_TEST_TASK_NAME, Test::class.java) {
            it.description = "Runs the $INTEGRATION_CONFIG_PREFIX tests."
            it.group = LifecycleBasePlugin.VERIFICATION_GROUP
            it.testClassesDirs = sourceSet.output.classesDirs
            it.classpath = sourceSet.runtimeClasspath
            it.mustRunAfter(JavaPlugin.TEST_TASK_NAME)
            it.onlyIf { !skipIntegrationTest(project) }
        }
        project.tasks.getByName(JavaBasePlugin.CHECK_TASK_NAME)
            .dependsOn(integrationTest)
    }
}
