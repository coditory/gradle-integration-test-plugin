package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import com.coditory.gradle.integration.TestSkippingConditions.skipIntegrationTest
import com.coditory.gradle.integration.TestSkippingConditions.skipUnitTest
import org.gradle.api.Project
import org.gradle.api.attributes.TestSuiteType
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.base.TestingExtension

@Suppress("UnstableApiUsage")
internal object TestSuitesConfiguration {
    private val isKotlinProject: Boolean by lazy {
        try {
            Class.forName("org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    fun apply(project: Project) {
        setupTestSuite(project)
        setupTestTask(project)
        if (isKotlinProject) {
            configureKotlinCompilation(project)
        }
    }

    private fun setupTestSuite(project: Project) {
        val testing = project.extensions.getByType(TestingExtension::class.java)
        val test = testing.suites.getByName("test") as JvmTestSuite
        test.targets.all { target ->
            target.testTask.configure { task ->
                task.onlyIf { !skipUnitTest(project) }
            }
        }
        testing.suites.register(INTEGRATION, JvmTestSuite::class.java) { testSuite ->
            testSuite.testType.set(TestSuiteType.INTEGRATION_TEST)
            testSuite.targets.all { target ->
                target.testTask.configure { task ->
                    task.shouldRunAfter(test)
                    task.onlyIf { !skipIntegrationTest(project) }
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

        project.configurations.getByName(integrationSourceSet.compileClasspathConfigurationName)
            .extendsFrom(project.configurations.getByName(testSourceSet.compileClasspathConfigurationName))

        project.configurations.getByName(integrationSourceSet.runtimeOnlyConfigurationName)
            .extendsFrom(project.configurations.getByName(testSourceSet.runtimeClasspathConfigurationName))

        project.configurations.getByName(integrationSourceSet.annotationProcessorConfigurationName)
            .extendsFrom(project.configurations.getByName(testSourceSet.annotationProcessorConfigurationName))

        integrationSourceSet.compileClasspath += testSourceSet.output + mainSourceSet.output
        integrationSourceSet.runtimeClasspath += testSourceSet.output + mainSourceSet.output
    }

    private fun setupTestTask(project: Project) {
        val integrationTestTask = project.tasks.create(INTEGRATION_TEST)
        integrationTestTask.description = "Runs integration test suites."
        integrationTestTask.group = LifecycleBasePlugin.VERIFICATION_GROUP
        integrationTestTask.onlyIf { !skipIntegrationTest(project) }
        integrationTestTask.dependsOn(INTEGRATION)
        project.tasks.getByName(JavaBasePlugin.CHECK_TASK_NAME)
            .dependsOn(INTEGRATION_TEST)
        project.tasks.getByName(JavaBasePlugin.CHECK_TASK_NAME)
            .dependsOn(INTEGRATION)
        project.tasks.getByName(INTEGRATION).onlyIf { !skipIntegrationTest(project) }
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
