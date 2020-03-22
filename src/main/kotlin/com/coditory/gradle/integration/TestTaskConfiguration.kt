package com.coditory.gradle.integration

import com.coditory.gradle.integration.TestSkippingConditions.skipTest
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

internal object TestTaskConfiguration {
    fun apply(project: Project) {
        val testTask = project.tasks.getByName(JavaPlugin.TEST_TASK_NAME)
        testTask.onlyIf { !skipTest(project) }
    }
}
