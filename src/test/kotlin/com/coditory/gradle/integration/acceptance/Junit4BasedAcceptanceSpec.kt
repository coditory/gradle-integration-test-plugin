package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.base.SpecProjectBuilder
import com.coditory.gradle.integration.base.SpecProjectRunner.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class Junit4BasedAcceptanceSpec {
    private val project = createProject()

    private fun createProject(): Project {
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
        return SpecProjectBuilder.project()
            .withBuildGradle(
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
            ).withFile(
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
            ).withFile(
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
            ).withFile("src/main/resources/a.txt", "main-a")
            .withFile("src/main/resources/b.txt", "main-b")
            .withFile("src/main/resources/c.txt", "main-c")
            .withFile("src/test/resources/b.txt", "test-b")
            .withFile("src/test/resources/c.txt", "test-c")
            .withFile("src/integration/resources/c.txt", "integration-c")
            .build()
    }

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = ["current", "5.0"])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        val result = runGradle(project, listOf("check", "--debug"), gradleVersion)
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
