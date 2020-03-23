package com.coditory.gradle.integration

import com.coditory.gradle.integration.acceptance.SampleProject.createProject
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.junit.jupiter.api.Test

class PluginSetupSpec {
    private val project: Project = createProject(JavaPlugin::class, IntegrationTestPlugin::class)

    @Test
    fun `should register plugin`() {
        assertThat(project.plugins.getPlugin(IntegrationTestPlugin.PLUGIN_ID))
            .isInstanceOf(IntegrationTestPlugin::class.java)
    }
}
