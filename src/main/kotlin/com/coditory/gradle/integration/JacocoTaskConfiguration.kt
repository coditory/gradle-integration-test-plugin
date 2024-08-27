package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST_TASK_NAME
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.testing.jacoco.tasks.JacocoReport

internal object JacocoTaskConfiguration {
    fun apply(project: Project) {
        if (project.pluginManager.hasPlugin("jacoco")) {
            project.jacocoTestReport {
                executionData(
                    project.file("${project.layout.buildDirectory.asFile.get()}/jacoco/${INTEGRATION_TEST_TASK_NAME}.exec"),
                )
            }
        }
    }
}

internal fun Project.jacocoTestReport(configuration: JacocoReport.() -> Unit) {
    configure<JacocoReport>(configuration)
}
