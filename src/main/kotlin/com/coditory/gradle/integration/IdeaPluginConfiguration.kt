package com.coditory.gradle.integration

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

object IdeaPluginConfiguration {
    fun apply(project: Project) {
        applyIdeaPluginIfNeeded(project)
        if (!project.plugins.hasPlugin(IdeaPlugin::class.java)) {
            return
        }
        val module = project.extensions.getByType(IdeaModel::class.java).module
        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val integrationTest = javaConvention.sourceSets
            .getByName(IntegrationTestPlugin.INTEGRATION_CONFIG_PREFIX)
        module.testSourceDirs = module.testSourceDirs + integrationTest.allSource.srcDirs
        module.testResourceDirs = module.testResourceDirs + integrationTest.resources.srcDirs
    }

    private fun applyIdeaPluginIfNeeded(project: Project) {
        if (project.plugins.hasPlugin(IdeaPlugin::class.java)) {
            // IdeaPlugin already applied
            return
        }
        if (project.rootProject.file(".idea").isDirectory) {
            project.plugins.apply(IdeaPlugin::class.java)
        }
    }
}
