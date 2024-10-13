package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.TEST_ALL_TASK_NAME
import com.coditory.gradle.integration.TestSkippingConditions.skipTestAll
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal object TestAllTaskConfiguration {
    fun apply(project: Project) {
        val testAllTask = project.tasks.create(TEST_ALL_TASK_NAME)
        testAllTask.description = "Runs all test suites."
        testAllTask.group = LifecycleBasePlugin.VERIFICATION_GROUP
        testAllTask.onlyIf { !skipTestAll(project) }
        project.tasks.withType(Test::class.java).forEach {
            testAllTask.dependsOn(it.name)
        }
        testAllTask.dependsOn(INTEGRATION_TEST)
    }
}
