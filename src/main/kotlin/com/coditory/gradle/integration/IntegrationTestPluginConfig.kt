package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.INTEGRATION_TEST
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.SKIP_INTEGRATION_TEST_FLAG_NAME
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.SKIP_TEST_ALL_FLAG_NAME
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.SKIP_UNIT_TEST_FLAG_NAME
import org.gradle.api.Project

internal data class IntegrationTestPluginConfig(
    val allTestTaskEnabled: Boolean,
    val unitTestsEnabled: Boolean,
    val integrationTestsEnabled: Boolean,
) {
    companion object {
        fun resolve(project: Project): IntegrationTestPluginConfig {
            return IntegrationTestPluginConfig(
                allTestTaskEnabled = !skipAllTests(project),
                unitTestsEnabled = !skipUnitTests(project),
                integrationTestsEnabled = !skipIntegrationTests(project),
            )
        }

        private fun skipAllTests(project: Project): Boolean {
            return hasTestAllFlag(project) ||
                (hasSkipUnitTestFlag(project) && hasSkipIntegrationTestFlag(project))
        }

        private fun skipUnitTests(project: Project): Boolean {
            return hasTestAllFlag(project) || hasSkipUnitTestFlag(project)
        }

        private fun skipIntegrationTests(project: Project): Boolean {
            return hasTestAllFlag(project) || hasSkipIntegrationTestFlag(project)
        }

        private fun hasTestAllFlag(project: Project): Boolean {
            return hasPropertyFlag(project, SKIP_TEST_ALL_FLAG_NAME)
        }

        private fun hasSkipUnitTestFlag(project: Project): Boolean {
            return hasPropertyFlag(project, SKIP_UNIT_TEST_FLAG_NAME)
        }

        private fun hasSkipIntegrationTestFlag(project: Project): Boolean {
            return hasPropertyFlag(project, SKIP_INTEGRATION_TEST_FLAG_NAME) ||
                hasExcludeIntegrationTestTaskParam(
                    project,
                )
        }

        private fun hasExcludeIntegrationTestTaskParam(project: Project): Boolean {
            return hasExcludedTask(project, INTEGRATION_TEST) || hasExcludedTask(project, INTEGRATION)
        }

        private fun hasPropertyFlag(project: Project, name: String): Boolean {
            if (project.properties.containsKey(name)) {
                val value = project.properties[name]
                return value == null || !value.toString().equals("false", true)
            }
            return false
        }

        private fun hasExcludedTask(project: Project, name: String): Boolean {
            return project.gradle.startParameter.excludedTaskNames.contains(name)
        }
    }
}
