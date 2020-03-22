package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.SampleProject.createProject
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.junit.jupiter.api.Test

class JacocoReportTaskConfigurationSpec {
    @Test
    fun `should register jacoco integrationTest tasks when jacoco plugin is enabled`() {
        val project = createProject(JavaPlugin::class, JacocoPlugin::class, IntegrationTestPlugin::class)
        assertThat(project.tasks.findByPath("jacocoIntegrationTestReport")).isNotNull()
        assertThat(project.tasks.findByPath("jacocoIntegrationTestCoverageVerification")).isNotNull()
    }

    @Test
    fun `should not register jacoco integrationTest tasks when jacoco plugin is disabled`() {
        val project = createProject(JavaPlugin::class, IntegrationTestPlugin::class)
        assertThat(project.tasks.findByPath("jacocoIntegrationTestReport")).isNull()
        assertThat(project.tasks.findByPath("jacocoIntegrationTestCoverageVerification")).isNull()
    }
}
