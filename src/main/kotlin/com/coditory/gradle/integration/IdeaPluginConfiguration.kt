package com.coditory.gradle.integration

import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModule

internal object IdeaPluginConfiguration {
    fun apply(project: Project) {
        applyIdeaPluginIfNeeded(project)
        if (!project.plugins.hasPlugin(IdeaPlugin::class.java)) {
            return
        }
        project.afterEvaluate {
            project.plugins.findPlugin(IdeaPlugin::class.java)
                ?.let { configureIdeaModule(project, it.model.module) }
        }
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

    private fun configureIdeaModule(project: Project, module: IdeaModule) {
        val integrationTest = SourceSetExtractor.sourceSets(project)
            .getByName(IntegrationTestPlugin.INTEGRATION_CONFIG_PREFIX)
        module.testSourceDirs = module.testSourceDirs + integrationTest.allSource.srcDirs
        module.testResourceDirs = module.testResourceDirs + integrationTest.resources.srcDirs
    }
}
