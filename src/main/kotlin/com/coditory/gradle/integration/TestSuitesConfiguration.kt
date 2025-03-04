package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.base.TestingExtension

@Suppress("UnstableApiUsage")
internal object TestSuitesConfiguration {
    private val isKotlinProject: Boolean by lazy {
        try {
            Class.forName("org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    fun apply(project: Project, config: IntegrationTestPluginConfig) {
        setupTestSuite(project, config)
        setupTestTask(project, config)
        if (isKotlinProject) {
            configureKotlinCompilation(project)
        }
    }

    private fun setupTestSuite(project: Project, config: IntegrationTestPluginConfig) {
        val testing = project.extensions.getByType(TestingExtension::class.java)
        val test = testing.suites.getByName("test") as JvmTestSuite
        test.targets.all { target ->
            target.testTask.configure { task ->
                task.enabled = config.unitTestsEnabled
            }
        }
        testing.suites.register(INTEGRATION, JvmTestSuite::class.java) { testSuite ->
            testSuite.targets.all { target ->
                target.testTask.configure { task ->
                    task.shouldRunAfter(test)
                    task.enabled = config.integrationTestsEnabled
                }
            }
            setupIntegrationSourceSet(project, testSuite)
        }
    }

    private fun setupIntegrationSourceSet(project: Project, testSuite: JvmTestSuite) {
        val sourceSets = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
        val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
        val integrationSourceSet = testSuite.sources

        project.configurations.getByName(integrationSourceSet.implementationConfigurationName)
            .extendsFrom(project.configurations.getByName(testSourceSet.implementationConfigurationName))

        project.configurations.getByName(integrationSourceSet.runtimeOnlyConfigurationName)
            .extendsFrom(project.configurations.getByName(testSourceSet.runtimeOnlyConfigurationName))

        project.configurations.getByName(integrationSourceSet.compileOnlyConfigurationName)
            .extendsFrom(project.configurations.getByName(testSourceSet.compileOnlyConfigurationName))

        project.configurations.getByName(integrationSourceSet.annotationProcessorConfigurationName)
            .extendsFrom(project.configurations.getByName(testSourceSet.annotationProcessorConfigurationName))

        integrationSourceSet.compileClasspath += testSourceSet.output + mainSourceSet.output
        integrationSourceSet.runtimeClasspath += testSourceSet.output + mainSourceSet.output
    }

    private fun setupTestTask(project: Project, config: IntegrationTestPluginConfig) {
        project.tasks.register(INTEGRATION_TEST, DummyTestTask::class.java) { integrationTestTask: Task ->
            integrationTestTask.description = "Runs integration test suites."
            integrationTestTask.group = LifecycleBasePlugin.VERIFICATION_GROUP
            integrationTestTask.enabled = config.integrationTestsEnabled
            integrationTestTask.dependsOn(INTEGRATION)
        }
        project.tasks.named(JavaBasePlugin.CHECK_TASK_NAME) { checkTask ->
            checkTask.dependsOn(INTEGRATION_TEST)
            checkTask.dependsOn(INTEGRATION)
        }
    }

    private fun configureKotlinCompilation(project: Project) {
        val kotlin = project.extensions
            .findByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java) ?: return
        kotlin.target.compilations.getByName(INTEGRATION) {
            val test = kotlin.target.compilations.getByName(SourceSet.TEST_SOURCE_SET_NAME)
            it.associateWith(test)
        }
    }
}

// Test task type required only for better intellij integration
// See:
// https://github.com/coditory/gradle-integration-test-plugin/pull/179
// https://github.com/coditory/gradle-integration-test-plugin/issues/181
//
// Thanks to DummyTestTask type Intellij:
// - displays testAll task results (unit + integration tests together) in a typical test tree
// - applies proper styles to the task in the gradle tasks window
// Drawbacks:
// - Gradle test configuration is executed for instances of Test and DummyTestTasks but there was no observable time penalty
abstract class DummyTestTask : Test() {
    override fun executeTests() {
        // deliberately empty
    }
}
