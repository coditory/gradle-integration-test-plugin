package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

internal object JacocoTaskConfiguration {
    fun apply(project: Project) {
        if (!project.pluginManager.hasPlugin("jacoco")) return
        project.tasks.withType(JacocoCoverageVerification::class.java).configureEach { task ->
            task.mustRunAfter(INTEGRATION)
        }
        project.tasks.withType(JacocoReport::class.java).configureEach { task ->
            task.mustRunAfter(INTEGRATION)
        }
        // execute only if integration test and jacoco are on the execution path
        // to preserve lazy task configuration
        project.gradle.taskGraph.whenReady {
            val names = project.gradle.taskGraph.allTasks.map { it.name }
            if (names.contains("jacocoTestReport") && names.contains(INTEGRATION)) {
                project.tasks.withType(JacocoReport::class.java)
                    .named("jacocoTestReport") { reportTask ->
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
