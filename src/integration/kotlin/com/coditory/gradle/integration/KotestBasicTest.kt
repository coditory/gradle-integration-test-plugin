package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProject
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotestBasicTest {
    companion object {
        @AutoClose
        private val project = createProject()

        @AutoClose
        private val failingProject = createProject(passingIntgTests = false)

        private fun createProject(passingIntgTests: Boolean = true): TestProject {
            val name = listOf(
                "project",
                KotestBasicTest::class.simpleName,
                if (passingIntgTests) "passing" else "failing",
            ).joinToString("-")
            return TestProjectBuilder
                .project(name)
                .withBuildGradleKts(
                    """
                    plugins {
                        kotlin("jvm") version "${Versions.kotlin}"
                        id("com.coditory.integration-test")
                    }

                    repositories {
                        mavenCentral()
                    }

                    dependencies {
                        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
                        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
                        testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
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
                    "src/integration/kotlin/TestIntgSpec.kt",
                    """
                    import io.kotest.core.spec.style.FreeSpec
                    import org.junit.jupiter.api.Assertions.assertEquals

                    class TestIntgSpec : FreeSpec({
                        "should pass" {
                            assertEquals(true, $passingIntgTests)
                        }
                    })
                    """,
                ).withFile(
                    "src/test/kotlin/TestUnitSpec.kt",
                    """
                    import io.kotest.core.spec.style.FreeSpec
                    import org.junit.jupiter.api.Assertions.assertEquals

                    class TestUnitSpec : FreeSpec({
                        "should pass" {
                            assertEquals(4, 2 + 2)
                        }
                    })
                    """,
                )
                .build()
        }
    }

    @ParameterizedTest(name = "should pass unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
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
