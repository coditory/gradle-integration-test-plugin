package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProject
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SpockBasicTest {
    companion object {
        @AutoClose
        private val project = createProject()

        @AutoClose
        private val failingProject = createProject(passingIntgTests = false)

        private fun createProject(passingIntgTests: Boolean = true): TestProject {
            val name = listOf(
                "project",
                SpockBasicTest::class.simpleName,
                if (passingIntgTests) "passing" else "failing",
            ).joinToString("-")
            return TestProjectBuilder
                .project(name)
                .withBuildGradle(
                    """
                    plugins {
                        id 'groovy'
                        id 'com.coditory.integration-test'
                    }

                    repositories {
                        mavenCentral()
                    }

                    dependencies {
                        testImplementation "org.spockframework:spock-core:${Versions.spock}"
                    }

                    tasks.withType(Test) {
                        testLogging {
                            events("passed", "failed", "skipped")
                            setExceptionFormat("full")
                        }
                    }
                    """,
                ).withFile(
                    "src/integration/groovy/TestIntgSpec.groovy",
                    """
                    import spock.lang.Specification

                    class TestIntgSpec extends Specification {
                        def "should pass"() {
                            expect:
                                $passingIntgTests
                        }
                    }
                    """,
                ).withFile(
                    "src/test/groovy/TestUnitSpec.groovy",
                    """
                    import spock.lang.Specification

                    class TestUnitSpec extends Specification {
                        def "should pass"() {
                            expect:
                                2 + 2 == 4
                        }
                    }
                    """,
                ).build()
        }
    }

    @ParameterizedTest(name = "should pass unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(SUCCESS)
    }

    @ParameterizedTest(name = "should fail integration tests on test failure for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should fail integration tests on test failure`(gradleVersion: String?) {
        // when
        val result = failingProject.runGradleAndFail(listOf("integration"), gradleVersion)
        // then
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
