package com.coditory.gradle.integration

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JvmTestSuitePlugin

@Suppress("UnstableApiUsage")
open class IntegrationTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(JavaPlugin::class.java)) {
            project.plugins.apply(JavaPlugin::class.java)
        }
        if (!project.plugins.hasPlugin(JvmTestSuitePlugin::class.java)) {
            project.plugins.apply(JvmTestSuitePlugin::class.java)
        }
        TestSuitesConfiguration.apply(project)
        TestAllTaskConfiguration.apply(project)
        JacocoTaskConfiguration.apply(project)
    }

    companion object {
        const val PLUGIN_ID = "com.coditory.integration-test"
        const val INTEGRATION_TEST = "integrationTest"
        const val TEST_ALL_TASK_NAME = "testAll"
        const val SKIP_UNIT_TEST_FLAG_NAME = "skipUnitTest"
        const val SKIP_INTEGRATION_TEST_FLAG_NAME = "skipIntegrationTest"
        const val SKIP_TEST_ALL_FLAG_NAME = "skipTest"
    }
}
