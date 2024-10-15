package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.TestProject
import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.project
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.junit.jupiter.api.Test

class JacocoConfigurationTest {
    private val project: TestProject = project()
        .withPlugins(JavaPlugin::class, JacocoPlugin::class, IntegrationTestPlugin::class)
        .build()

    @Test
    fun `should register integrationTest exec files in jacoco report task`() {
        val jacocoReportTask = project.tasks.getByName("jacocoTestReport") as JacocoReport
        val executionData = jacocoReportTask.executionData
        assertThat(executionData).isNotNull()
        assertThat(executionData.asPath).isEqualTo(
            project.toBuildPath(
                "jacoco/test.exec",
                "jacoco/integration.exec",
            ),
        )
    }
}
