package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import org.gradle.api.Project
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

internal object JacocoTaskConfiguration {
    fun apply(project: Project) {
        if (project.pluginManager.hasPlugin("jacoco")) {
            var dstFile: String? = null
            project.tasks.named(INTEGRATION_TEST) { task ->
                val jacocoTaskExtension = task.extensions.getByType(JacocoTaskExtension::class.java)
                dstFile = jacocoTaskExtension.destinationFile?.path
            }
            if (dstFile != null) {
                project.tasks.withType(JacocoReport::class.java) { task ->
                    task.executionData(dstFile)
                    task.mustRunAfter(INTEGRATION_TEST)
                }
                project.tasks.withType(JacocoCoverageVerification::class.java) { task ->
                    task.mustRunAfter(INTEGRATION_TEST)
                }
            }
        }
    }
}
