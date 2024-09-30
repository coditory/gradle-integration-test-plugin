package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.project
import com.coditory.gradle.integration.base.TestProjectRunner.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinInternalScopeAcceptanceTest {
    private val project = createProject()

    private fun createProject(): Project {
        val commonImports =
            """
            import org.junit.jupiter.api.Test
            import org.junit.jupiter.api.Assertions.assertEquals
            import InternalObject
            import PublicObject
            """.trimIndent()
        return project("sample-project")
            .withKtsBuildGradle(
                """
                plugins {
                    kotlin("jvm") version "2.0.20"
                    id("com.coditory.integration-test")
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
                    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
                    // sample integration test dependency
                    integrationTestImplementation("com.coditory.quark:quark-context:0.1.22")
                }

                tasks.withType<Test> {
                    useJUnitPlatform()
                    testLogging {
                        events("passed", "failed", "skipped")
                        setExceptionFormat("full")
                    }
                }
                """,
            ).withFile(
                "src/main/kotlin/PublicObject.kt",
                """
                object PublicObject {
                    val SOME_VALUE = "Public"
                }
                """,
            ).withFile(
                "src/main/kotlin/InternalObject.kt",
                """
                object InternalObject {
                    val SOME_VALUE = "Internal"
                }
                """,
            ).withFile(
                "src/integrationTest/kotlin/TestIntgSpec.kt",
                """
                $commonImports

                class TestIntgSpec {
                    @Test
                    fun shouldSeePublicObjectOnClasspath() {
                        assertEquals("Public", PublicObject.SOME_VALUE)
                    }

                    @Test
                    fun shouldSeeInternalObjectOnClasspath() {
                        assertEquals("Internal", InternalObject.SOME_VALUE)
                    }
                }
                """,
            ).withFile(
                "src/test/kotlin/TestUnitSpec.kt",
                """
                $commonImports

                class TestUnitSpec {
                    @Test
                    fun shouldSeePublicObjectOnClasspath() {
                        assertEquals("Public", PublicObject.SOME_VALUE)
                    }

                    @Test
                    fun shouldSeeInternalObjectOnClasspath() {
                        assertEquals("Internal", InternalObject.SOME_VALUE)
                    }
                }
                """,
            ).build()
    }

    @ParameterizedTest(name = "should make internal scope visible in integration tests {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = runGradle(project, listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
