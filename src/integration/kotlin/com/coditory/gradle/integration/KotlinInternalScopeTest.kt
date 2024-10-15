package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinInternalScopeTest {
    companion object {
        @AutoClose
        private val project = TestProjectBuilder
            .project("project-${KotlinInternalScopeTest::class.simpleName}")
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
                "src/integration/kotlin/TestIntgSpec.kt",
                """
                import org.junit.jupiter.api.Test
                import org.junit.jupiter.api.Assertions.assertEquals
                import InternalObject
                import PublicObject

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
                import org.junit.jupiter.api.Test
                import org.junit.jupiter.api.Assertions.assertEquals
                import InternalObject
                import PublicObject

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
            )
            .build()
    }

    @ParameterizedTest(name = "should make internal scope visible in integration tests for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
