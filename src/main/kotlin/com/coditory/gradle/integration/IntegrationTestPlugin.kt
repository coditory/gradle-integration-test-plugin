package com.coditory.gradle.integration

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.testing.jacoco.plugins.JacocoPlugin

open class IntegrationTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(JavaPlugin::class.java)) {
            project.plugins.apply(JavaPlugin::class.java)
        }
        setupPlugin(project)
    }

    private fun setupPlugin(project: Project) {
        val testTaskProvider = setupTestTask(project)
        if (project.plugins.hasPlugin(JacocoPlugin::class.java)) {
            setupJacocoTasks(testTaskProvider, project)
        }
    }

    private fun setupTestTask(project: Project): TaskProvider<*> {
        return IntegrationTestSetConfigurator(project)
            .setupTestSet(INTEGRATION_TEST_TASK_NAME, INTEGRATION_CONFIG_PREFIX)
    }

    private fun setupJacocoTasks(taskProvider: TaskProvider<*>, project: Project) {
        JacocoReportTaskConfigurator(project)
            .configureDefaultJacocoTask(taskProvider)
    }

    companion object {
        const val PLUGIN_ID = "com.coditory.integration-test"
        const val INTEGRATION_TEST_TASK_NAME = "integrationTest"
        const val INTEGRATION_CONFIG_PREFIX = "integration"
    }
}
