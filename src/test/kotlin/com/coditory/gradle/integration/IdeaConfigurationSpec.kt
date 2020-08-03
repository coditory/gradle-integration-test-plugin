package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.SpecProjectBuilder.Companion.project
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.junit.jupiter.api.Test
import java.io.File

class IdeaConfigurationSpec {
    @Test
    fun `should automatically apply idea plugin when there is idea folder`() {
        val project = project()
            .withDirectory(".idea")
            .withPlugins(IntegrationTestPlugin::class)
            .build()
        assertThat(hasIdeaPlugin(project)).isTrue()
    }

    @Test
    fun `should not apply idea plugin when there is no idea folder`() {
        val project = project()
            .withPlugins(IntegrationTestPlugin::class)
            .build()
        assertThat(hasIdeaPlugin(project)).isFalse()
    }

    @Test
    fun `should add integration tests to idea module test dirs`() {
        val project = project()
            .withPlugins(IdeaPlugin::class, IntegrationTestPlugin::class)
            .build()
        val ideaPlugin = getIdeaPlugin(project)
        val module = ideaPlugin.model.module
        assertThat(module.testSourceDirs)
            .containsAll(toIntegrationSrcFiles(project, "java"))
        assertThat(module.testResourceDirs)
            .containsAll(toIntegrationSrcFiles(project, "resources"))
    }

    @Test
    fun `should add integration tests from groovy and scala to idea module test dirs`() {
        val project = project()
            .withPlugins(IdeaPlugin::class, IntegrationTestPlugin::class, GroovyPlugin::class, ScalaPlugin::class)
            .build()
        val ideaPlugin = getIdeaPlugin(project)
        val module = ideaPlugin.model.module
        assertThat(module.testSourceDirs)
            .containsAll(toIntegrationSrcFiles(project, "java", "groovy", "scala"))
        assertThat(module.testResourceDirs)
            .containsAll(toIntegrationSrcFiles(project, "resources"))
    }

    private fun hasIdeaPlugin(project: Project): Boolean {
        return project.plugins.hasPlugin(IdeaPlugin::class.java)
    }

    private fun getIdeaPlugin(project: Project): IdeaPlugin {
        return project.plugins.getPlugin(IdeaPlugin::class.java)
    }

    private fun toIntegrationSrcFiles(project: Project, vararg paths: String): Set<File> {
        return paths
            .map { "${project.projectDir}/src/integration/$it" }
            .map { File(it) }
            .toSet()
    }
}
