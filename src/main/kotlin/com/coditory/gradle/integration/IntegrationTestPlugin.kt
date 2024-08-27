package com.coditory.gradle.integration

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

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
        JacocoTaskConfiguration.apply(project)
    }

    companion object {
        const val PLUGIN_ID = "com.coditory.integration-test"
        const val INTEGRATION_CONFIG_PREFIX = "integration"
        const val INTEGRATION_TEST_TASK_NAME = "integrationTest"
        const val TEST_ALL_TASK_NAME = "testAll"
        const val SKIP_TEST_FLAG_NAME = "skipUnitTests"
        const val SKIP_TEST_ALL_FLAG_NAME = "skipTests"
        const val SKIP_INTEGRATION_TEST_FLAG_NAME = "skipIntegrationTests"
    }
}
