package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class JacocoBasedTest {
    companion object {
        @AutoClose
        private val project = TestProjectBuilder
            .project("project-" + JacocoBasedTest::class.simpleName)
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
                    testImplementation "org.junit.jupiter:junit-jupiter-api:${Versions.junit}"
                    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:${Versions.junit}"
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
                "src/integration/java/TestIntgSpec.java",
                """
                import org.junit.jupiter.api.Test;
                import static org.junit.jupiter.api.Assertions.assertEquals;

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
                import org.junit.jupiter.api.Test;
                import static org.junit.jupiter.api.Assertions.assertEquals;

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
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should aggregate coverage from unit and integration tests when using Jacoco`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check", "jacocoTestReport"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(project.readFileFromBuildDir("reports/jacoco/test/jacocoTestReport.xml"))
            // missed method is the init
            .contains("<counter type=\"METHOD\" missed=\"1\" covered=\"2\"/>")
    }
}
