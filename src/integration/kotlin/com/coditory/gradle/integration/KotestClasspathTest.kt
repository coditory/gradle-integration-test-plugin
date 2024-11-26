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

class KotestClasspathTest {
    companion object {
        @AutoClose
        private val project = createProject()

        private fun createProject(): TestProject {
            val builder = TestProjectBuilder
                .project("project-${KotestClasspathTest::class.simpleName}")
                .withBuildGradleKts(
                    """
                    plugins {
                        kotlin("jvm") version "${Versions.kotlin}"
                        id("com.coditory.integration-test")
                    }

                    repositories {
                        mavenCentral()
                    }

                    dependencies {
                        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
                        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
                        testImplementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
                        // sample integration test dependency
                        integrationImplementation("com.google.code.gson:gson:${Versions.gson}")
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
                    "src/test/kotlin/ClasspathFileReader.kt",
                    """
                    import java.net.URI
                    import java.nio.file.Files
                    import java.nio.file.Paths

                    object ClasspathFileReader {
                        fun readFile(name: String): String {
                            val uri: URI = ClasspathFileReader::class.java.classLoader
                                ?.getResource(name)
                                ?.toURI()!!
                            return Files.readString(Paths.get(uri))
                        }
                    }
                    """,
                ).withFile(
                    "src/integration/kotlin/TestIntgSpec.kt",
                    """
                    import io.kotest.core.spec.style.FreeSpec
                    import org.junit.jupiter.api.Assertions.assertEquals
                    import org.junit.jupiter.api.Assertions.assertDoesNotThrow
                    import ClasspathFileReader.readFile

                    class TestIntgSpec : FreeSpec({
                        "should read a.txt from main" {
                            assertEquals("main-a", readFile("a.txt"))
                        }

                        "should read b.txt from test" {
                            assertEquals("test-b", readFile("b.txt"))
                        }

                        "should read c.txt from integration" {
                            assertEquals("integration-c", readFile("c.txt"))
                        }

                        "should read constant value A from main" {
                            assertEquals("main-a", ConstantValuesA.MODULE)
                        }

                        "should read constant value B from test" {
                             assertEquals("test-b", ConstantValuesB.MODULE)
                        }

                        "should read constant value C from integration" {
                            assertEquals("integration-c", ConstantValuesC.MODULE)
                        }

                        "should resolve integration dependency" {
                            assertDoesNotThrow { Class.forName("com.google.gson.Gson") }
                        }
                    })
                    """,
                ).withFile(
                    "src/test/kotlin/TestUnitSpec.kt",
                    """
                    import io.kotest.core.spec.style.FreeSpec
                    import org.junit.jupiter.api.Assertions.assertEquals
                    import ClasspathFileReader.readFile

                    class TestUnitSpec : FreeSpec({
                        "should read a.txt from main" {
                            assertEquals("main-a", readFile("a.txt"))
                        }

                        "should read b.txt from test" {
                            assertEquals("test-b", readFile("b.txt"))
                        }

                        "should read constant value A from main" {
                            assertEquals("main-a", ConstantValuesA.MODULE)
                        }

                        "should read constant value B from test" {
                             assertEquals("test-b", ConstantValuesB.MODULE)
                        }
                    })
                    """,
                )
            listOf("main").forEach {
                builder
                    .withFile("src/$it/resources/a.txt", "$it-a")
                    .withFile(
                        "src/$it/kotlin/ConstantValuesA.kt",
                        """
                        object ConstantValuesA {
                            const val MODULE: String = "$it-a";
                        }
                        """,
                    )
            }
            listOf("main", "test").forEach {
                builder
                    .withFile("src/$it/resources/b.txt", "$it-b")
                    .withFile(
                        "src/$it/kotlin/ConstantValuesB.kt",
                        """
                        object ConstantValuesB {
                            const val MODULE: String = "$it-b";
                        }
                        """,
                    )
            }
            listOf("main", "test", "integration").forEach {
                builder
                    .withFile("src/$it/resources/c.txt", "$it-c")
                    .withFile(
                        "src/$it/kotlin/ConstantValuesC.kt",
                        """
                        object ConstantValuesC {
                            const val MODULE: String = "$it-c";
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
