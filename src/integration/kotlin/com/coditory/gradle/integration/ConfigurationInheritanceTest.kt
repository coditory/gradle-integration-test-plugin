package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ConfigurationInheritanceTest {
    companion object {
        @AutoClose
        private val project = TestProjectBuilder
            .project("project-${ConfigurationInheritanceTest::class.simpleName}")
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

                testing {
                    suites {
                        register<JvmTestSuite>("customTest")
                    }
                }

                val customTestImplementation by configurations.getting {
                    extendsFrom(configurations.integrationImplementation.get())
                }
                val customTestRuntimeOnly by configurations.getting {

                    extendsFrom(configurations.integrationRuntimeOnly.get())
                }

                dependencies {
                    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit}")
                    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
                    // sample dependency
                    implementation("com.google.code.gson:gson:${Versions.gson}")
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
                "src/customTest/java/TestSpec.java",
                """
                import org.junit.jupiter.api.Test;
                import com.google.gson.Gson;

                public class TestSpec {
                    @Test
                    void shouldResolveCompileDependencyFromMainConfig() {
                        // Gson class import is the actual test
                    }
                }
                """,
            )
            .build()
    }

    @AfterEach
    fun cleanProject() {
        project.clean()
    }

    @ParameterizedTest(name = "should resolve compile dependencies in custom test suite for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should resolve compile dependencies in custom test suite`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("customTest"), gradleVersion)
        // then
        assertThat(result.task(":customTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
