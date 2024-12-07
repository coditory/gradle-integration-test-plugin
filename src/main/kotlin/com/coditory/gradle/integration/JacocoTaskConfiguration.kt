package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

internal object JacocoTaskConfiguration {
    private const val JACOCO_PLUGIN = "jacoco"
    private const val JACOCO_REPORT_TASK = "jacocoTestReport"

    fun apply(project: Project) {
        if (!project.pluginManager.hasPlugin(JACOCO_PLUGIN)) return
        project.tasks.withType(JacocoCoverageVerification::class.java).configureEach { task ->
            task.mustRunAfter(INTEGRATION)
        }
        project.tasks.withType(JacocoReport::class.java).configureEach { task ->
            task.mustRunAfter(INTEGRATION)
        }
        // execute only if integration tests or jacocoTestReport are on the execution path
        // to preserve lazy task configuration
        project.gradle.taskGraph.whenReady {
            val names = project.gradle.taskGraph.allTasks.map { it.name }
            if (names.contains(JACOCO_REPORT_TASK) || names.contains(INTEGRATION)) {
                project.tasks.withType(JacocoReport::class.java)
                    .named(JACOCO_REPORT_TASK) { reportTask ->
                        val jacocoTaskExtension =
                            project.tasks.getByName(INTEGRATION).extensions.getByType(JacocoTaskExtension::class.java)
                        val dstFile = jacocoTaskExtension.destinationFile?.path
                        if (dstFile != null) {
                            reportTask.executionData(dstFile)
                        }
                    }
            }
        }
    }
}
