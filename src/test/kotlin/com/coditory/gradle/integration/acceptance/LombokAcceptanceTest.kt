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

class LombokAcceptanceTest {
    private val project = createProject()

    private fun createProject(): Project {
        val commonImports =
            """
            import org.junit.jupiter.api.Test;

            import static org.junit.jupiter.api.Assertions.assertEquals;
            import static org.junit.jupiter.api.Assertions.assertNotEquals;
            """.trimIndent()
        return project("sample-lombok-project")
            .withBuildGradle(
                """
                plugins {
                    id 'com.coditory.integration-test'
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    compileOnly "org.projectlombok:lombok:1.18.34"
                    annotationProcessor "org.projectlombok:lombok:1.18.34"
                    testCompileOnly "org.projectlombok:lombok:1.18.34"
                    testAnnotationProcessor "org.projectlombok:lombok:1.18.34"
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
                """,
            ).withFile(
                "src/main/java/MainValueExample.java",
                """
                import lombok.Value;

                @Value
                public class MainValueExample {
                    private final String name;
                }
                """,
            ).withFile(
                "src/test/java/TestValueExample.java",
                """
                import lombok.Value;

                @Value
                public class TestValueExample {
                    private final String name;
                }
                """,
            ).withFile(
                "src/integrationTest/java/IntgValueExample.java",
                """
                import lombok.Value;

                @Value
                public class IntgValueExample {
                    private final String name;
                }
                """,
            ).withFile(
                "src/integrationTest/java/TestIntgSpec.java",
                """
                $commonImports

                public class TestIntgSpec {
                    @Test
                    public void shouldValueObjectsFromMain() {
                        assertEquals(new MainValueExample("X"), new MainValueExample("X"));
                        assertNotEquals(new MainValueExample("X"), new MainValueExample("Y"));
                    }

                    @Test
                    public void shouldValueObjectsFromTest() {
                        assertEquals(new TestValueExample("X"), new TestValueExample("X"));
                        assertNotEquals(new TestValueExample("X"), new TestValueExample("Y"));
                    }

                    @Test
                    public void shouldValueObjectsFromIntegration() {
                        assertEquals(new IntgValueExample("X"), new IntgValueExample("X"));
                        assertNotEquals(new IntgValueExample("X"), new IntgValueExample("Y"));
                    }
                }
                """,
            ).withFile(
                "src/test/java/TestUnitSpec.java",
                """
                $commonImports

                public class TestUnitSpec {
                    @Test
                    public void shouldValueObjectsFromMain() {
                        assertEquals(new MainValueExample("X"), new MainValueExample("X"));
                        assertNotEquals(new MainValueExample("X"), new MainValueExample("Y"));
                    }

                    @Test
                    public void shouldValueObjectsFromTest() {
                        assertEquals(new TestValueExample("X"), new TestValueExample("X"));
                        assertNotEquals(new TestValueExample("X"), new TestValueExample("Y"));
                    }
                }
                """,
            ).build()
    }

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests with lombok`(gradleVersion: String?) {
        // when
        val result = runGradle(project, listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
