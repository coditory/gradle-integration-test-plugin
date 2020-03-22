package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_CONFIG_PREFIX
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST_TASK_NAME
import com.coditory.gradle.integration.base.SampleProject.createProject
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.junit.jupiter.api.Test
import org.gradle.api.tasks.testing.Test as TestTask

class IntegrationTestTaskConfigurationSpec {
    private val project: Project = createProject(JavaPlugin::class, IntegrationTestPlugin::class)

    @Test
    fun `should configure integration source sets`() {
        val sourceSet = getSourceSet()
        assertThat(sourceSet).isNotNull
        assertThat(sourceSet.output.classesDirs.asPath).isEqualTo(toBuildPath("classes/java/integration"))
        assertThat(sourceSet.output.resourcesDir.toString()).isEqualTo(toBuildPath("resources/integration"))
        val expectedBuildPath = toBuildPath(
            listOf(
                "classes/java/integration",
                "resources/integration",
                "classes/java/test",
                "resources/test",
                "classes/java/main",
                "resources/main"
            )
        )
        assertThat(sourceSet.runtimeClasspath.asPath)
            .isEqualTo(expectedBuildPath)
    }

    @Test
    fun `should configure integrationTest task`() {
        val integrationSourceSet = getSourceSet()
        val task = getTestTask()
        assertThat(task.testClassesDirs).isNotNull
        assertThat(task.description).isEqualTo("Runs the integration tests.")
        assertThat(task.group).isEqualTo(LifecycleBasePlugin.VERIFICATION_GROUP)
        assertThat(task.testClassesDirs).isEqualTo(integrationSourceSet.output.classesDirs)
        assertThat(task.classpath).isEqualTo(integrationSourceSet.runtimeClasspath)
        assertThat(task.enabled).isEqualTo(true)
    }

    @Test
    fun `should configure integrationTest task to run after check`() {
        val checkTask = project.tasks.getByName(JavaBasePlugin.CHECK_TASK_NAME)
        val dependencies = checkTask.dependsOn
            .filterIsInstance<TaskProvider<*>>()
            .map { it.name }
        assertThat(dependencies).contains(INTEGRATION_TEST_TASK_NAME)
    }

    @Test
    fun `should add groovy paths to source sets when groovy plugin is enabled`() {
        val project = createProject(GroovyPlugin::class, IntegrationTestPlugin::class)
        val expectedBuildPath = toBuildPath(
            listOf(
                "classes/java/integration",
                "classes/groovy/integration",
                "resources/integration",
                "classes/java/test",
                "classes/groovy/test",
                "resources/test",
                "classes/java/main",
                "classes/groovy/main",
                "resources/main"
            ),
            project
        )
        assertThat(getSourceSet(project).runtimeClasspath.asPath)
            .isEqualTo(expectedBuildPath)
    }

    private fun getTestTask(): TestTask {
        return project.tasks.getByName(INTEGRATION_TEST_TASK_NAME) as TestTask
    }

    private fun toBuildPath(path: String, project: Project = this.project): String {
        return toBuildPath(listOf(path), project)
    }

    private fun toBuildPath(paths: List<String>, project: Project = this.project): String {
        return paths.joinToString(":") { "${project.buildDir}/$it" }
    }

    private fun getSourceSet(project: Project = this.project): SourceSet {
        return project.convention.getPlugin(JavaPluginConvention::class.java)
            .sourceSets.getByName(INTEGRATION_CONFIG_PREFIX)
    }
}
