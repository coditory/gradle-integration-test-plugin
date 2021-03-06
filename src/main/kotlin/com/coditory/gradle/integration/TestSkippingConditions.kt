package com.coditory.gradle.integration

import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.SKIP_INTEGRATION_TEST_FLAG_NAME
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.SKIP_TEST_ALL_FLAG_NAME
import com.coditory.gradle.integration.IntegrationTestPlugin.Companion.SKIP_TEST_FLAG_NAME
import org.gradle.api.Project

internal object TestSkippingConditions {
    fun skipTestAll(project: Project): Boolean {
        return hasTestAllFlag(project) ||
            (hasSkipTestFlag(project) && hasSkipIntegrationTestFlag(project))
    }

    fun skipTest(project: Project): Boolean {
        return hasTestAllFlag(project) || hasSkipTestFlag(project)
    }

    fun skipIntegrationTest(project: Project): Boolean {
        return hasTestAllFlag(project) || hasSkipIntegrationTestFlag(project)
    }

    private fun hasTestAllFlag(project: Project): Boolean {
        return hasPropertyFlag(project, SKIP_TEST_ALL_FLAG_NAME)
    }

    private fun hasSkipTestFlag(project: Project): Boolean {
        return hasPropertyFlag(project, SKIP_TEST_FLAG_NAME)
    }

    private fun hasSkipIntegrationTestFlag(project: Project): Boolean {
        return hasPropertyFlag(project, SKIP_INTEGRATION_TEST_FLAG_NAME)
    }

    private fun hasPropertyFlag(project: Project, name: String): Boolean {
        if (project.properties.containsKey(name)) {
            val value = project.properties[name]
            return value == null || !value.toString().equals("false", true)
        }
        return false
    }
}
