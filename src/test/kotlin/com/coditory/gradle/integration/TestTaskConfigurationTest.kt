package com.coditory.gradle.integration

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
import java.io.File
import org.gradle.api.tasks.testing.Test as TestTask

class TestTaskConfigurationTest {
    private val project: Project = createProject()

    @Test
    fun `should configure integration source sets`() {
        val sourceSet = getSourceSet()
        assertThat(sourceSet).isNotNull
        assertThat(sourceSet.output.classesDirs.asPath).isEqualTo(toBuildPath("classes/java/integrationTest"))
        assertThat(sourceSet.output.resourcesDir.toString()).isEqualTo(toBuildPath("resources/integrationTest"))
        // TODO: Fix it. Tried is all. It's failing with Could not find org.gradle.internal.impldep.org.junit.jupiter:junit-jupiter:5.8.2
        // Tried: adding repositories to test project, defining tests to use junit platform etc - did not help...
        // assertThat(sourceSet.runtimeClasspath.asPath)
        //     .isEqualTo(
        //         toBuildPath(
        //             listOf(
        //                 "classes/java/integrationTest",
        //                 "resources/integrationTest",
        //                 "classes/java/test",
        //                 "resources/test",
        //                 "classes/java/main",
        //                 "resources/main",
        //             ),
        //         ),
        //     )
    }

    @Test
    fun `should configure integrationTest task`() {
        val integrationSourceSet = getSourceSet()
        val task = getTestTask()
        assertThat(task.testClassesDirs).isNotNull
        assertThat(task.description).isEqualTo("Runs the integration test suite.")
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
        return project.tasks.getByName(INTEGRATION_TEST) as TestTask
    }

    private fun toBuildPath(path: String, project: Project = this.project): String {
        return toBuildPath(listOf(path), project)
    }

    private fun toBuildPath(paths: List<String>, project: Project = this.project): String {
        return paths.joinToString(File.pathSeparator) {
            "${project.layout.buildDirectory.get()}${File.separator}${it.replace("/", File.separator)}"
        }
    }

    @Suppress("UnstableApiUsage")
    private fun getSourceSet(project: Project = this.project): SourceSet {
        return project.extensions.getByType(TestingExtension::class.java).suites
            .getByName(INTEGRATION_TEST)
            .let { it as JvmTestSuite }
            .sources
    }
}