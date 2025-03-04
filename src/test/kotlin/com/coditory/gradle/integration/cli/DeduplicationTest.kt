package com.coditory.gradle.integration.cli

import com.coditory.gradle.integration.base.TestProject
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.Test

class DeduplicationTest {
    companion object {
        const val TEST_CONFIGURATION_LOG = "Test config"

        @AutoClose
        private val project = createProject()

        private fun createProject(): TestProject {
            val name = "project-${DeduplicationTest::class.simpleName}"
            return TestProjectBuilder
                .project(name)
                .withBuildGradleKts(
                    """
                    plugins {
                        id("com.coditory.integration-test")
                    }

                    repositories {
                        mavenCentral()
                    }

                    dependencies {
                        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
                        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
                    }

                    tasks.withType<Test>().configureEach {
                        println("$TEST_CONFIGURATION_LOG - " + this.name)
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
                    import static org.junit.jupiter.api.Assertions.assertEquals;
                    import org.junit.jupiter.api.Test;

                    public class TestIntgSpec {
                        @Test
                        public void shouldPassIntegration() {
                            assertEquals(true, true);
                        }
                    }
                    """,
                ).withFile(
                    "src/test/java/TestUnitSpec.java",
                    """
                    import static org.junit.jupiter.api.Assertions.assertEquals;
                    import org.junit.jupiter.api.Test;

                    public class TestUnitSpec {
                        @Test
                        public void shouldPassUnit() {
                            assertEquals(true, true);
                        }
                    }
                    """,
                )
                .build()
        }
    }

    @AfterEach
    fun cleanProjects() {
        project.clean()
    }

    @Test
    fun `should not duplicate test runs and test configuration on check command`() {
        // when
        val result = project.runGradle(listOf("check"))
        // then all tests pass and run once
        assertDeduplicatedSuccessfulTests(result, "check")
        // and test configuration was executed 3 times for each test task
        assertTestConfigurationsForTasks(result, listOf("integration", "integrationTest", "test"))
    }

    @Test
    fun `should not duplicate test runs and test configuration on testAll command`() {
        // when
        val result = project.runGradle(listOf("testAll"))
        // then all tests pass and run once
        assertDeduplicatedSuccessfulTests(result, "testAll")
        // and test configuration was executed 4 times for each test task
        assertTestConfigurationsForTasks(result, listOf("testAll", "integration", "integrationTest", "test"))
    }

    private fun assertDeduplicatedSuccessfulTests(result: BuildResult, lastTask: String) {
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":$lastTask")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        // and test task runs only unit tests
        assertLogsBetweenTasks(result, "test", "compileIntegrationJava")
            .containsExactly("TestUnitSpec > shouldPassUnit() PASSED")
            .doesNotContain("TestIntgSpec > shouldPassIntegration()")

        // and integration task runs only intg tests
        assertLogsBetweenTasks(result, "integration", "integrationTest")
            .containsExactly("TestIntgSpec > shouldPassIntegration() PASSED")
            .doesNotContain("TestUnitSpec > shouldPassUnit()")

        // and integrationTest task runs no tests (only depends on integration task)
        assertLogsBetweenTasks(result, "integrationTest", lastTask)
            .doesNotContain("TestUnitSpec > shouldPassIntegration()")
            .doesNotContain("TestUnitSpec > shouldPassUnit()")
    }

    private fun assertLogsBetweenTasks(result: BuildResult, task: String, nextTask: String): ListAssert<String> {
        val logs = result.output.substring(
            result.output.indexOf("> Task :$task\n"),
            result.output.indexOf("> Task :$nextTask\n"),
        ).split("\n").drop(2).filterNot { it.isEmpty() }
        return assertThat(logs)
    }

    private fun assertTestConfigurationsForTasks(result: BuildResult, tasks: List<String>) {
        val testConfigRuns = result.output
            .windowed(TEST_CONFIGURATION_LOG.length, 1)
            .count { it == TEST_CONFIGURATION_LOG }
        assertThat(testConfigRuns).isEqualTo(tasks.size)
        tasks.forEach {
            assertThat(result.output).contains("$TEST_CONFIGURATION_LOG - $it")
        }
    }
}
