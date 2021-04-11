package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.createProject
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.junit.jupiter.api.Test

class PluginSetupTest {
    private val project: Project = createProject()

    @Test
    fun `should register plugin`() {
        assertThat(project.plugins.getPlugin(IntegrationTestPlugin.PLUGIN_ID))
            .isInstanceOf(IntegrationTestPlugin::class.java)
    }
}
