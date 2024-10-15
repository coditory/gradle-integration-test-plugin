package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.createProject
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.gradle.testing.base.TestingExtension
import org.junit.jupiter.api.Test
import org.gradle.api.tasks.testing.Test as TestTask

class TestTaskConfigurationTest {
    private val project = createProject()

    @Test
    fun `should configure integrationTest task`() {
        val integrationSourceSet = getSourceSet()
        val task = getTestTask()
        assertThat(task.testClassesDirs).isNotNull
        assertThat(task.description).isEqualTo("Runs the integration suite.")
        assertThat(task.group).isEqualTo(VERIFICATION_GROUP)
        assertThat(task.testClassesDirs).isEqualTo(integrationSourceSet.output.classesDirs)
        assertThat(task.classpath).isEqualTo(integrationSourceSet.runtimeClasspath)
        assertThat(task.enabled).isEqualTo(true)
    }

    @Test
    fun `should configure integrationTest task to run after check`() {
        val checkTask = project.tasks.getByName(JavaBasePlugin.CHECK_TASK_NAME)
        val dependencies = checkTask.dependsOn
            .filterIsInstance<String>()
        assertThat(dependencies).contains(INTEGRATION_TEST)
    }

    private fun getTestTask(): TestTask {
        return project.tasks.getByName(INTEGRATION) as TestTask
    }

    @Suppress("UnstableApiUsage")
    private fun getSourceSet(project: Project = this.project): SourceSet {
        return project.extensions.getByType(TestingExtension::class.java).suites
            .getByName(INTEGRATION)
            .let { it as JvmTestSuite }
            .sources
    }
}
