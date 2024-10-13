package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.TEST_ALL_TASK_NAME
import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.createProject
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin.TEST_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.junit.jupiter.api.Test

class TestAllTaskConfigurationTest {
    private val project: Project = createProject()

    @Test
    fun `should configure testAll task`() {
        val task = getTestAllTask()
        assertThat(task.dependsOn).isEqualTo(setOf(TEST_TASK_NAME, INTEGRATION, INTEGRATION_TEST))
        assertThat(task.description).isEqualTo("Runs all test suites.")
        assertThat(task.group).isEqualTo(VERIFICATION_GROUP)
        assertThat(task.enabled).isEqualTo(true)
    }

    private fun getTestAllTask(): Task {
        return project.tasks.getByName(TEST_ALL_TASK_NAME)
    }
}
