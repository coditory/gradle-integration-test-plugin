package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.project
import com.coditory.gradle.integration.base.TestProjectRunner.runGradle
import com.coditory.gradle.integration.base.readFileFromBuildDir
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class JacocoBasedAcceptanceTest {
    private val project = createProject()

    private fun createProject(): Project {
        val commonImports =
            """
            import org.junit.jupiter.api.Test;

            import static org.junit.jupiter.api.Assertions.assertEquals;
            """.trimIndent()
        return project("sample-project")
            .withBuildGradle(
                """
                plugins {
                    id 'jacoco'
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

                jacocoTestReport {
                    reports {
                        xml.required = true
                    }
                }
                """,
            ).withFile(
                "src/main/java/Calculator.java",
                """
                public class Calculator {
                    public static int add(int a, int b) {
                        return a + b;
                    }

                    public static int subtract(int a, int b) {
                        return a - b;
                    }
                }
                """,
            ).withFile(
                "src/integrationTest/java/TestIntgSpec.java",
                """
                $commonImports

                public class TestIntgSpec {
                    @Test
                    public void shouldSubtract() {
                        assertEquals(3, Calculator.subtract(6, 3));
                    }
                }
                """,
            ).withFile(
                "src/test/java/TestUnitSpec.java",
                """
                $commonImports

                public class TestUnitSpec {
                    @Test
                    public void shouldAdd() {
                        assertEquals(9, Calculator.add(6, 3));
                    }
                }
                """,
            )
            .build()
    }

    @ParameterizedTest(name = "should aggregate coverage from unit and integration tests when using Jacoco {0}")
    // @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION])
    fun `should aggregate coverage from unit and integration tests when using Jacoco`(gradleVersion: String?) {
        // when
        val result =
            runGradle(project, listOf("check", "jacocoTestReport"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(project.readFileFromBuildDir("reports/jacoco/test/jacocoTestReport.xml"))
            // missed method is the init
            .contains("<counter type=\"METHOD\" missed=\"1\" covered=\"2\"/>")
    }
}
