package com.coditory.gradle.integration.cli

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProject
import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SpockClasspathTest {
    companion object {
        @AutoClose
        private val project = createProject()

        private fun createProject(): TestProject {
            val builder = TestProjectBuilder
                .project("project-${SpockClasspathTest::class.simpleName}")
                .withBuildGradle(
                    """
                    plugins {
                        id 'groovy'
                        id 'com.coditory.integration-test'
                    }

                    repositories {
                        mavenCentral()
                    }

                    dependencies {
                        testImplementation "org.spockframework:spock-core:${Versions.spock}"
                        // sample integration test dependency
                        integrationImplementation "com.google.code.gson:gson:${Versions.gson}"
                    }

                    tasks.withType(Test) {
                        testLogging {
                            events("passed", "failed", "skipped")
                            setExceptionFormat("full")
                        }
                    }
                    """,
                ).withFile(
                    "src/test/groovy/ClasspathFileReader.groovy",
                    """
                    class ClasspathFileReader {
                        static String readFile(String name) throws Exception {
                            return ClasspathFileReader.class.getResource("/" + name).getText()
                        }
                    }
                    """,
                ).withFile(
                    "src/integration/groovy/TestIntgSpec.groovy",
                    """
                    import spock.lang.Specification
                    import static ClasspathFileReader.readFile;

                    class TestIntgSpec extends Specification {
                        def "should read a.txt from main"() {
                            expect:
                                readFile("a.txt") == "main-a";
                        }

                        def "should read b.txt from test"() {
                            expect:
                                readFile('b.txt') == 'test-b'
                        }

                        def "should read c.txt from integration"() {
                            expect:
                                readFile('c.txt') == 'integration-c'
                        }

                        def "should read constant value A from main"() {
                            expect:
                                ConstantValuesA.MODULE == 'main-a'
                        }

                        def "should read constant value B from test"() {
                            expect:
                                ConstantValuesB.MODULE == 'test-b'
                        }

                        def "should read constant value C from integration"() {
                            expect:
                                ConstantValuesC.MODULE == 'integration-c'
                        }

                        def "should resolve integration dependency"() {
                            when:
                                Class.forName("com.google.gson.Gson")
                            then:
                                noExceptionThrown()
                        }
                    }
                    """,
                ).withFile(
                    "src/test/groovy/TestUnitSpec.groovy",
                    """
                    import spock.lang.Specification
                    import static ClasspathFileReader.readFile

                    class TestUnitSpec extends Specification {
                        def "should read a.txt from main"() {
                            expect:
                                readFile('a.txt') == 'main-a'
                        }

                        def "should read b.txt from test"() {
                            expect:
                                readFile('b.txt') == 'test-b'
                        }

                        def "should read constant value A from main"() {
                            expect:
                                ConstantValuesA.MODULE == 'main'
                        }

                        def "should read constant value B from test"() {
                            expect:
                                ConstantValuesB.MODULE == 'test'
                        }
                    }
                    """,
                )
            val lang = { module: String -> if (module == "main") "java" else "groovy" }
            listOf("main").forEach {
                builder
                    .withFile("src/$it/resources/a.txt", "$it-a")
                    .withFile(
                        "src/$it/${lang(it)}/ConstantValuesA.${lang(it)}",
                        """
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
                        "src/$it/${lang(it)}/ConstantValuesB.${lang(it)}",
                        """
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
                        "src/$it/${lang(it)}/ConstantValuesC.${lang(it)}",
                        """
                        public class ConstantValuesC {
                            public static final String MODULE = "$it-c";
                        }
                        """,
                    )
            }
            return builder.build()
        }
    }

    @AfterEach
    fun cleanProject() {
        project.clean()
    }

    @ParameterizedTest(name = "should read files from classpath for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = project.runGradle(listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(SUCCESS)
    }
}
