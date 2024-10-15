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

class JUnitClasspathTest {
    companion object {
        @AutoClose
        private val project = createProject()

        private fun createProject(): TestProject {
            val builder = TestProjectBuilder
                .project("project-${JUnitClasspathTest::class.simpleName}")
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
                        // sample integration test dependency
                        integrationImplementation("com.google.code.gson:gson:${Versions.gson}")
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
                    "src/test/java/sample/ClasspathFileReader.java",
                    """
                    package sample;

                    import java.io.IOException;
                    import java.net.URI;
                    import java.net.URISyntaxException;
                    import java.nio.file.Files;
                    import java.nio.file.Path;
                    import java.nio.file.Paths;

                    public class ClasspathFileReader {
                        public static String readFile(String name) {
                            try {
                                URI uri = ClasspathFileReader.class.getClassLoader()
                                    .getResource(name)
                                    .toURI();
                                Path path = Paths.get(uri);
                                return Files.readString(path);
                            } catch (IOException | URISyntaxException e) {
                                throw new RuntimeException("Could not read file from classpath: " + name, e);
                            }
                        }
                    }
                    """,
                ).withFile(
                    "src/integration/java/sample/TestIntgSpec.java",
                    """
                    package sample;

                    import org.junit.jupiter.api.Test;
                    import static org.junit.jupiter.api.Assertions.assertEquals;
                    import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
                    import static sample.ClasspathFileReader.readFile;

                    public class TestIntgSpec {
                        @Test
                        public void shouldReadATxtFileFromMain() {
                            assertEquals("main-a", readFile("a.txt"));
                        }

                        @Test
                        public void shouldReadBTxtFileFromTest() {
                            assertEquals("test-b", readFile("b.txt"));
                        }

                        @Test
                        public void shouldReadCTxtFileFromIntegration() {
                            assertEquals("integration-c", readFile("c.txt"));
                        }

                        @Test
                        public void shouldReadConstantValueAFromMain() {
                            assertEquals("main-a", ConstantValuesA.MODULE);
                        }

                        @Test
                        public void shouldReadConstantValueBFromTest() {
                            assertEquals("test-b", ConstantValuesB.MODULE);
                        }

                        @Test
                        public void shouldReadConstantValueCFromIntegration() {
                            assertEquals("integration-c", ConstantValuesC.MODULE);
                        }

                        @Test
                        void shouldResolveIntegrationDependency() {
                            assertDoesNotThrow(() -> Class.forName("com.google.gson.Gson"));
                        }
                    }
                    """,
                ).withFile(
                    "src/test/java/sample/TestUnitSpec.java",
                    """
                    package sample;

                    import org.junit.jupiter.api.Test;
                    import static org.junit.jupiter.api.Assertions.assertEquals;
                    import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
                    import static sample.ClasspathFileReader.readFile;

                    public class TestUnitSpec {
                        @Test
                        public void shouldReadATxtFromMain() {
                            assertEquals("main-a", readFile("a.txt"));
                        }

                        @Test
                        public void shouldReadBTxtFromTest() {
                            assertEquals("test-b", readFile("b.txt"));
                        }

                        @Test
                        public void shouldReadConstantValueAFromMain() {
                            assertEquals("main-a", ConstantValuesA.MODULE);
                        }

                        @Test
                        public void shouldReadConstantValueBFromTest() {
                            assertEquals("test-b", ConstantValuesB.MODULE);
                        }
                    }
                    """,
                )
            listOf("main").forEach {
                builder
                    .withFile("src/$it/resources/a.txt", "$it-a")
                    .withFile(
                        "src/$it/java/sample/ConstantValuesA.java",
                        """
                        package sample;

                        public class ConstantValuesA {
                            public static final String MODULE = "$it-a";
                        }
                        """,
                    )
            }
            listOf("main", "test").forEach {
                builder
                    .withFile("src/$it/resources/b.txt", "$it-b")
                    .withFile(
                        "src/$it/java/sample/ConstantValuesB.java",
                        """
                        package sample;

                        public class ConstantValuesB {
                            public static final String MODULE = "$it-b";
                        }
                        """,
                    )
            }
            listOf("main", "test", "integration").forEach {
                builder
                    .withFile("src/$it/resources/c.txt", "$it-c")
                    .withFile(
                        "src/$it/java/sample/ConstantValuesC.java",
                        """
                        package sample;

                        public class ConstantValuesC {
                            public static final String MODULE = "$it-c";
                        }
                        """,
                    )
            }
            return builder.build()
        }
    }

    @ParameterizedTest(name = "should read files from classpath for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integration")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
