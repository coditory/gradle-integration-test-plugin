package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.project
import com.coditory.gradle.integration.base.TestProjectRunner.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class Junit5BasedAcceptanceTest {
    private val project = createProject()

    private fun createProject(): Project {
        val commonImports =
            """
            import org.junit.jupiter.api.Test;
            
            import static org.junit.jupiter.api.Assertions.assertEquals;
            import static base.ClasspathFileReader.readFile;
            """.trimIndent()
        return project("sample-project")
            .withBuildGradle(
                """
                plugins {
                    id 'com.coditory.integration-test'
                }
    
                repositories {
                    mavenCentral()
                }
    
                dependencies {
                    testImplementation "org.junit.jupiter:junit-jupiter-api:5.5.1"
                    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:5.5.1"
                }
    
                test {
                    useJUnitPlatform()
                    testLogging {
                        events("passed", "failed", "skipped")
                        setExceptionFormat("full")
                    }
                }
                """
            ).withFile(
                "src/test/java/base/ClasspathFileReader.java",
                """
                package base;
                
                import java.net.URI;
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
                            return new String(Files.readAllBytes(path));
                        } catch (Exception e) {
                            throw new RuntimeException("Could not read file from classpath: " + name, e);
                        }
                    }
                }
                """
            ).withFile(
                "src/test/java/ConstantValues.java",
                """
                public class ConstantValues {
                    public static final String MODULE = "test";
                }
                """
            ).withFile(
                "src/integration/java/ConstantValues.java",
                """
                public class ConstantValues {
                    public static final String MODULE = "integration";
                }
                """
            ).withFile(
                "src/main/java/ConstantValues.java",
                """
                public class ConstantValues {
                    public static final String MODULE = "main";
                }
                """
            ).withFile(
                "src/main/java/MainConstantValues.java",
                """
                public class MainConstantValues {
                    public static final String MODULE = "main";
                }
                """
            ).withFile(
                "src/integration/java/TestIntgSpec.java",
                """
                $commonImports
    
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
                    public void shouldReadCTxtFileFromTest() {
                        assertEquals("integration-c", readFile("c.txt"));
                    }
    
                    @Test
                    public void shouldReadConstantValueFromIntModule() {
                        assertEquals("integration", ConstantValues.MODULE);
                    }
                    
                    @Test
                    public void shouldReadConstantValueFromMainModule() {
                        assertEquals("main", MainConstantValues.MODULE);
                    }
                }
                """
            ).withFile(
                "src/test/java/TestUnitSpec.java",
                """
                $commonImports
    
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
                    public void shouldReadConstantValueFromTestModule() {
                        assertEquals("test", ConstantValues.MODULE);
                    }
                    
                    @Test
                    public void shouldReadConstantValueFromMainModule() {
                        assertEquals("main", MainConstantValues.MODULE);
                    }
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
    @ValueSource(strings = ["current", "5.0", "6.0"])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = runGradle(project, listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
