package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProject
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CommandLineTest {
    companion object {
        @AutoClose
        private val project = createProject()

        @AutoClose
        private val failingProject = createProject(passingIntgTests = false)

        private fun createProject(passingIntgTests: Boolean = true): TestProject {
            val name = listOf(
                "project",
                CommandLineTest::class.simpleName,
                if (passingIntgTests) "passing" else "failing",
            ).joinToString("-")
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
                        public void shouldPass() {
                            assertEquals(true, $passingIntgTests);
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
                        public void shouldPass() {
                            assertEquals(true, true);
                        }
                    }
                    """,
                )
                .build()
        }
    }

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @ParameterizedTest(name = "should run integration tests on integrationTest command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run integration tests on integrationTest command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("integrationTest"), gradleVersion)
        // then
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @ParameterizedTest(name = "should run integration tests on integration command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run integration tests on integration command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("integration"), gradleVersion)
        // then
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `should not run integration tests during test task`() {
        // when
        val result = project.runGradle(listOf("test"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isNull()
    }

    @Test
    fun `should run integration tests and unit tests during testAll task`() {
        // when
        val result = project.runGradle(listOf("testAll"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `should exclude integration tests on -x integrationTest`() {
        // when
        val result = project.runGradle(listOf("check", "-x", "integrationTest"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should fail check command when integration tests fail`() {
        // when
        val result = failingProject.runGradleAndFail(listOf("check"))
        // then
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":check")?.outcome).isNull()
    }

    @Test
    fun `should skip integration tests -PskipIntegrationTest`() {
        // when
        val result = project.runGradle(listOf("check", "-PskipIntegrationTest"))
        // then
        assertThat(result.task(":check")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should skip all tests on -PskipTest`() {
        // when
        val result = project.runGradle(listOf("check", "-PskipTest"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
    }

    @Test
    fun `should skip unit tests on -PskipUnitTest`() {
        // when
        val result = project.runGradle(listOf("check", "-PskipUnitTest"))
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SKIPPED)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @ParameterizedTest(name = "task {0} should be cacheable by gradle configuration cache")
    @ValueSource(strings = ["test", "integrationTest", "testAll", "check"])
    fun `should be cacheable by gradle configuration cache`(task: String?) {
        // when
        val result = project.runGradle(listOf("--configuration-cache", task!!))
        // then
        assertThat(result.task(":$task")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
