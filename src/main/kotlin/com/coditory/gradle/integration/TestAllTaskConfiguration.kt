package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST_TASK_NAME
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.TEST_ALL_TASK_NAME
import com.coditory.gradle.integration.TestSkippingConditions.skipTestAll
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal object TestAllTaskConfiguration {
    fun apply(project: Project) {
        val testAllTask = project.tasks.create(TEST_ALL_TASK_NAME)
        testAllTask.description = "Runs all tests."
        testAllTask.group = LifecycleBasePlugin.VERIFICATION_GROUP
        testAllTask.dependsOn(INTEGRATION_TEST_TASK_NAME, TEST_TASK_NAME)
        testAllTask.onlyIf { !skipTestAll(project) }
    }
}
