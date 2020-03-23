package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.acceptance.SampleProject.createBuildGradle
import com.coditory.gradle.integration.acceptance.SampleProject.createProjectFile
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CommandLineAcceptanceSpec {
    private val projectDir = createTempDir()

    @BeforeEach
    fun setupProject() {
        createBuildGradle(
            projectDir,
            """
            plugins {
                id 'com.coditory.integration-test'
            }

            repositories {
                jcenter()
            }

            dependencies {
                testImplementation "org.junit.jupiter:junit-jupiter-api:5.5.1"
                testRuntime "org.junit.jupiter:junit-jupiter-engine:5.5.1"
            }

            test {
                useJUnitPlatform()
            }
            """
        )
        val commonImports =
            """
            import static org.junit.jupiter.api.Assertions.assertEquals;
            import org.junit.jupiter.api.Test;
            """.trimIndent()
        createProjectFile(
            projectDir,
            "src/integration/java/TestIntgSpec.java",
            """
            $commonImports

            public class TestIntgSpec {
                @Test
                public void shouldPassSampleIntegrationSpec() {
                    assertEquals(true, true);
                }

                @Test
                public void shouldPassSecondSampleIntegrationSpec() {
                    assertEquals(true, true);
                }
            }
            """
        )
        createProjectFile(
            projectDir,
            "src/test/java/TestUnitSpec.java",
            """
            $commonImports

            public class TestUnitSpec {
                @Test
                public void shouldPassSampleUnitSpec() {
                    assertEquals(true, true);
                }
            }
            """
        )
    }

    @AfterEach
    fun removeProjectDir() {
        projectDir.deleteRecursively()
    }

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = ["current", "5.0"])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        val result = runGradle(listOf("check", "--debug"), gradleVersion)
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `should not run integration tests during test task`() {
        val result = runGradle(listOf("test"))
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isNull()
    }

    @Test
    fun `should run integration tests and unit tests during testAll task`() {
        val result = runGradle(listOf("testAll"))
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `should skip exclude integration tests`() {
        val result = runGradle(listOf("check", "-x", "integrationTest"))
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isNull()
    }

    @Test
    fun `should skip integration tests`() {
        val result = runGradle(listOf("check", "-PskipIntegrationTests"))
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should skip all tests`() {
        val result = runGradle(listOf("check", "-PskipTests"))
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should skip unit tests`() {
        val result = runGradle(listOf("check", "-PskipUnitTests"))
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun runGradle(arguments: List<String>, gradleVersion: String? = null): BuildResult {
        val builder = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(arguments)
            .withPluginClasspath()
            .forwardOutput()
        if (!gradleVersion.isNullOrBlank() && gradleVersion != "current") {
            builder.withGradleVersion(gradleVersion)
        }
        return builder.build()
    }
}
