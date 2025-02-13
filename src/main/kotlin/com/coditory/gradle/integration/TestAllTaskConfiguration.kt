package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.TEST_ALL_TASK_NAME
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal object TestAllTaskConfiguration {
    fun apply(project: Project, config: IntegrationTestPluginConfig) {
        val testTasks = project.tasks.withType(Test::class.java).names
        project.tasks.register(TEST_ALL_TASK_NAME, DummyTestTask::class.java) { testAllTask: Task ->
            testAllTask.description = "Runs all test suites."
            testAllTask.group = LifecycleBasePlugin.VERIFICATION_GROUP
            testAllTask.enabled = config.allTestTaskEnabled
            testTasks.forEach {
                testAllTask.dependsOn(it)
            }
            testAllTask.dependsOn(INTEGRATION_TEST)
        }
    }
}
