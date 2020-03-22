package com.coditory.gradle.integration

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin

open class IntegrationTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(JavaPlugin::class.java)) {
            project.plugins.apply(JavaPlugin::class.java)
        }
        setupPlugin(project)
    }

    private fun setupPlugin(project: Project) {
        IntegrationTestTaskConfiguration.apply(project)
        TestTaskConfiguration.apply(project)
        TestAllTaskConfiguration.apply(project)
        if (project.plugins.hasPlugin(JacocoPlugin::class.java)) {
            JacocoReportTaskConfiguration.apply(project)
        }
    }

    companion object {
        const val PLUGIN_ID = "com.coditory.integration-test"
        const val INTEGRATION_CONFIG_PREFIX = "integration"
        const val INTEGRATION_TEST_TASK_NAME = "integrationTest"
        const val TEST_ALL_TASK_NAME = "testAll"
        const val SKIP_TEST_FLAG_NAME = "skipTest"
        const val SKIP_TEST_ALL_FLAG_NAME = "skipTestAll"
        const val SKIP_INTEGRATION_TEST_FLAG_NAME = "skipIntegrationTest"
    }
}
