package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST_TASK_NAME
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.Report
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

internal object JacocoReportTaskConfiguration {
    fun apply(project: Project) {
        val task = project.tasks.getByName(INTEGRATION_TEST_TASK_NAME)
        addDefaultReportTask(project, task)
        addDefaultCoverageVerificationTask(project, task)
    }

    private fun addDefaultReportTask(project: Project, task: Task) {
        project.tasks.register(
                "jacoco${task.name.capitalize()}Report",
                JacocoReport::class.java
        ) {
            val extension: JacocoPluginExtension = project.extensions.getByType(JacocoPluginExtension::class.java)
            it.group = LifecycleBasePlugin.VERIFICATION_GROUP
            it.description = "Generates code coverage report for the ${task.name} task."
            it.executionData(task)
            it.sourceSets(getMainSourceSet(project))
            it.reports.all { report -> setupReportDestination(report, extension, project, task, it) }
        }
    }

    private fun setupReportDestination(
            report: ConfigurableReport,
            extension: JacocoPluginExtension,
            project: Project,
            task: Task,
            it: JacocoReport
    ) {
        if (report.outputType == Report.OutputType.DIRECTORY) {
            report.setDestination(
                    project.provider {
                        File(extension.reportsDir, task.name + "/" + report.name)
                    }
            )
        } else {
            report.setDestination(
                    project.provider {
                        File(extension.reportsDir, task.name + "/" + it.name + "." + report.name)
                    }
            )
        }
    }

    private fun addDefaultCoverageVerificationTask(project: Project, task: Task) {
        project.tasks.register(
                "jacoco${task.name.capitalize()}CoverageVerification",
                JacocoCoverageVerification::class.java
        ) {
            it.group = LifecycleBasePlugin.VERIFICATION_GROUP
            it.description = "Verifies code coverage metrics based on specified rules for the ${task.name} task."
            it.executionData(task)
            it.sourceSets(getMainSourceSet(project))
        }
    }

    private fun getMainSourceSet(project: Project): SourceSet {
        return project.extensions
                .getByType(SourceSetContainer::class.java)
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    }
}
