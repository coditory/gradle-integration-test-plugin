package com.coditory.gradle.integration.cli

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PlatformDependencyTest {
    companion object {
        @AutoClose
        private val project = TestProjectBuilder
            .project("project-${PlatformDependencyTest::class.simpleName}")
            .withBuildGradleKts(
                """
                plugins {
                    id("com.coditory.integration-test")
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    implementation(platform("org.springframework.boot:spring-boot-dependencies:${Versions.spring}"))
                    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
                    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
                    integrationImplementation("org.springframework.boot:spring-boot-starter-test")
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
                import org.junit.jupiter.api.Test;
                import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

                public class TestIntgSpec {
                    @Test
                    void shouldResolveDependencyFromBom() {
                        assertDoesNotThrow(() -> Class.forName("org.springframework.test.context.ContextConfiguration"));
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

    @ParameterizedTest(name = "should use dependency version from platform dependency for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
