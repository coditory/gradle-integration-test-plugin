package com.coditory.gradle.integration

import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin

internal object IdeaPluginConfiguration {
    fun apply(project: Project) {
        applyIdeaPluginIfNeeded(project)
        if (!project.plugins.hasPlugin(IdeaPlugin::class.java)) {
            return
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
}
