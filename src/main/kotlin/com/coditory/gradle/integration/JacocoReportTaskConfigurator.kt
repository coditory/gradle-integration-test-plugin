package com.coditory.gradle.integration

import org.gradle.api.Project
import org.gradle.api.reporting.Report
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

internal class JacocoReportTaskConfigurator(private val project: Project) {
  fun configureDefaultJacocoTask(taskProvider: TaskProvider<*>) {
    addDefaultReportTask(taskProvider)
    addDefaultCoverageVerificationTask(taskProvider)
  }

  private fun addDefaultReportTask(testTaskProvider: TaskProvider<*>) {
    project.tasks.register(
      "jacoco${testTaskProvider.name.capitalize()}Report",
      JacocoReport::class.java
    ) {
      val task = testTaskProvider.get()
      val extension = project.extensions.getByType(JacocoPluginExtension::class.java)
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP
      it.description = "Generates code coverage report for the ${testTaskProvider.name} task."
      it.executionData(task)
      it.sourceSets(getMainSourceSet())
      it.reports.all { report ->
        if (report.outputType == Report.OutputType.DIRECTORY) {
          report.setDestination(project.provider { File(extension.reportsDir, testTaskProvider.name + "/" + report.name) })
        } else {
          report.setDestination(project.provider { File(extension.reportsDir, testTaskProvider.name + "/" + it.name + "." + report.name) })
        }
      }
    }
  }

  private fun addDefaultCoverageVerificationTask(testTaskProvider: TaskProvider<*>) {
    project.tasks.register(
      "jacoco${testTaskProvider.name.capitalize()}CoverageVerification",
      JacocoCoverageVerification::class.java
    ) {
      it.group = LifecycleBasePlugin.VERIFICATION_GROUP
      it.description = "Verifies code coverage metrics based on specified rules for the ${testTaskProvider.name} task."
      it.executionData(testTaskProvider.get())
      it.sourceSets(getMainSourceSet())
    }
  }

  private fun getMainSourceSet(): SourceSet {
    return project.extensions
      .getByType(SourceSetContainer::class.java)
      .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
  }
}
