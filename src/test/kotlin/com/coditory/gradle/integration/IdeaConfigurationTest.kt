package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.project
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.junit.jupiter.api.Test
import java.io.File

class IdeaConfigurationTest {
    @Test
    fun `should automatically apply idea plugin when there is idea folder`() {
        // when
        val project = project()
            .withDirectory(".idea")
            .withPlugins(IntegrationTestPlugin::class)
            .build()

        // then
        assertThat(hasIdeaPlugin(project)).isTrue()
    }

    @Test
    fun `should not apply idea plugin when there is no idea folder`() {
        // when
        val project = project()
            .withPlugins(IntegrationTestPlugin::class)
            .build()

        // then
        assertThat(hasIdeaPlugin(project)).isFalse()
    }

    private fun hasIdeaPlugin(project: Project): Boolean {
        return project.plugins.hasPlugin(IdeaPlugin::class.java)
    }

    @Suppress("unused")
    private fun getIdeaPlugin(project: Project): IdeaPlugin {
        return project.plugins.getPlugin(IdeaPlugin::class.java)
    }

    @Suppress("unused")
    private fun toIntegrationSrcFiles(project: Project, vararg paths: String): Set<File> {
        return paths
            .map { "${project.projectDir}/src/integration/$it" }
            .map { File(it) }
            .toSet()
    }
}
