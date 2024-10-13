package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder
import com.coditory.gradle.integration.base.TestProjectRunner.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CommandLineAcceptanceTest {
    private val project = createProject()

    private fun createProject(): Project {
        val commonImports =
            """
            import static org.junit.jupiter.api.Assertions.assertEquals;
            import org.junit.jupiter.api.Test;
            """.trimIndent()
        return TestProjectBuilder
            .project()
            .withBuildGradle(
                """
                plugins {
                    id 'com.coditory.integration-test'
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    testImplementation "org.junit.jupiter:junit-jupiter-api:5.11.0"
                    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:5.11.0"
                }

                tasks.withType(Test) {
                    useJUnitPlatform()
                    testLogging {
                        events("passed", "failed", "skipped")
                        setExceptionFormat("full")
                    }
                }
                """,
            ).withFile(
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
                """,
            ).withFile(
                "src/test/java/TestUnitSpec.java",
                """
                $commonImports

                public class TestUnitSpec {
                    @Test
                    public void shouldPassSampleUnitSpec() {
                        assertEquals(true, true);
                    }
                }
                """,
            ).build()
    }

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = runGradle(project, listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @ParameterizedTest(name = "should run integration tests on integrationTest command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run integration tests on integrationTest command`(gradleVersion: String?) {
        // when
        val result = runGradle(project, listOf("integrationTest"), gradleVersion)
        // then
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `should not run integration tests during test task`() {
        // when
        val result = runGradle(project, listOf("test"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isNull()
    }

    @Test
    fun `should run integration tests and unit tests during testAll task`() {
        // when
        val result = runGradle(project, listOf("testAll"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `should exclude integration tests`() {
        // when
        val result = runGradle(project, listOf("check", "-x", "integrationTest"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should skip integration tests`() {
        // when
        val result = runGradle(project, listOf("check", "-PskipIntegrationTest"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should skip all tests`() {
        // when
        val result = runGradle(project, listOf("check", "-PskipTest"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should skip unit tests`() {
        // when
        val result = runGradle(project, listOf("check", "-PskipUnitTest"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
