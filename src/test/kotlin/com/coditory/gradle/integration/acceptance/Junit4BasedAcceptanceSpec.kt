package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.acceptance.SampleProject.createBuildGradle
import com.coditory.gradle.integration.acceptance.SampleProject.createProjectFile
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class Junit4BasedAcceptanceSpec {
    private val projectDir = createTempDir()

    @BeforeEach
    fun setupProject() {
        createBuildGradle(
            projectDir,
            """
            plugins {
                id 'com.coditory.integration-test'
            }

            repositories {
                jcenter()
            }

            dependencies {
                testCompile "junit:junit:4.12"
            }
            """
        )
        val commonImports =
            """
            import static org.junit.Assert.assertEquals;
            import org.junit.Test;
            import java.nio.file.Files;
            import java.nio.file.Paths;
            import java.nio.file.Path;
            """.trimIndent()
        val readFileMethod =
            """
            private String readFile(String name) throws Exception {
                Path path = Paths.get(getClass().getClassLoader().getResource(name).toURI());
                return Files.readString(path);
            }
            """.trimIndent()
        createProjectFile(
            projectDir,
            "src/integration/java/TestIntgSpec.java",
            """
            $commonImports

            public class TestIntgSpec {
                @Test
                public void shouldReadATxtFileFromMain() throws Exception {
                    assertEquals("main-a", readFile("a.txt"));
                }

                @Test
                public void shouldReadBTxtFileFromTest() throws Exception {
                    assertEquals("test-b", readFile("b.txt"));
                }

                @Test
                public void shouldReadCTxtFileFromTest() throws Exception {
                    assertEquals("integration-c", readFile("c.txt"));
                }

                $readFileMethod
            }
            """
        )
        createProjectFile(
            projectDir,
            "src/test/java/TestUnitSpec.java",
            """
            $commonImports

            public class TestUnitSpec {
                @Test
                public void shouldReadATxtFromMain() throws Exception {
                    assertEquals("main-a", readFile("a.txt"));
                }

                @Test
                public void shouldReadBTxtFromTest() throws Exception {
                    assertEquals("test-b", readFile("b.txt"));
                }

                $readFileMethod
            }
            """
        )
        createProjectFile(projectDir, "src/main/resources/a.txt", "main-a")
        createProjectFile(projectDir, "src/main/resources/b.txt", "main-b")
        createProjectFile(projectDir, "src/main/resources/c.txt", "main-c")
        createProjectFile(projectDir, "src/test/resources/b.txt", "test-b")
        createProjectFile(projectDir, "src/test/resources/c.txt", "test-c")
        createProjectFile(projectDir, "src/integration/resources/c.txt", "integration-c")
    }

    @AfterEach
    fun removeProjectDir() {
        projectDir.deleteRecursively()
    }

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = ["current", "5.0"])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        val result = runGradle(listOf("check", "--debug"), gradleVersion)
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    private fun runGradle(arguments: List<String>, gradleVersion: String? = null): BuildResult {
        val builder = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(arguments)
            .withPluginClasspath()
            .forwardOutput()
        if (!gradleVersion.isNullOrBlank() && gradleVersion != "current") {
            builder.withGradleVersion(gradleVersion)
        }
        return builder.build()
    }
}
