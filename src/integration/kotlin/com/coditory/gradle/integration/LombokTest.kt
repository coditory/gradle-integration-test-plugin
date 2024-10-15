package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class LombokTest {
    companion object {
        @AutoClose
        private val project = TestProjectBuilder
            .project("project-" + LombokTest::class.simpleName)
            .withBuildGradle(
                """
                plugins {
                    id 'com.coditory.integration-test'
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    compileOnly "org.projectlombok:lombok:${Versions.lombok}"
                    annotationProcessor "org.projectlombok:lombok:${Versions.lombok}"
                    testCompileOnly "org.projectlombok:lombok:${Versions.lombok}"
                    testAnnotationProcessor "org.projectlombok:lombok:${Versions.lombok}"
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
                "src/integration/java/IntgValueExample.java",
                """
                import lombok.Value;

                @Value
                public class IntgValueExample {
                    private final String name;
                }
                """,
            ).withFile(
                "src/integration/java/TestIntgSpec.java",
                """
                import org.junit.jupiter.api.Test;

                import static org.junit.jupiter.api.Assertions.assertEquals;
                import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
                import org.junit.jupiter.api.Test;

                import static org.junit.jupiter.api.Assertions.assertEquals;
                import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
            )
            .build()
    }

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests with lombok`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
