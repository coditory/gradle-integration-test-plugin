package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MAX_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.GradleTestVersions.GRADLE_MIN_SUPPORTED_VERSION
import com.coditory.gradle.integration.base.TestProjectBuilder.Companion.project
import com.coditory.gradle.integration.base.TestProjectRunner.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SpockBasedAcceptanceTest {
    private val project =
        project("sample-project")
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
                    testImplementation "org.spockframework:spock-core:2.4-M4-groovy-4.0"
                    // sample integration test dependency
                    integrationTestImplementation "org.slf4j:slf4j-api:2.0.16"
                }

                test {
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
                "src/test/groovy/ConstantValues.groovy",
                """
                class ConstantValues {
                    static final String MODULE = "test"
                }
                """,
            ).withFile(
                "src/integrationTest/groovy/ConstantValues.groovy",
                """
                class ConstantValues {
                    static final String MODULE = "integration"
                }
                """,
            ).withFile(
                "src/main/java/ConstantValues.java",
                """
                public class ConstantValues {
                    public static final String MODULE = "main";
                }
                """,
            ).withFile(
                "src/main/java/MainConstantValues.java",
                """
                public class MainConstantValues {
                    public static final String MODULE = "main";
                }
                """,
            ).withFile(
                "src/integrationTest/groovy/TestIntgSpec.groovy",
                """
                import spock.lang.Specification
                import static ClasspathFileReader.readFile

                class TestIntgSpec extends Specification {
                    def "should read a.txt from main"() {
                        expect:
                            readFile('a.txt') == 'main-a'
                    }

                    def "should read b.txt from test"() {
                        expect:
                            readFile('b.txt') == 'test-b'
                    }

                    def "should read c.txt from test"() {
                        expect:
                            readFile('c.txt') == 'integration-c'
                    }

                    def "should read constant value from integration module"() {
                        expect:
                            ConstantValues.MODULE == 'integration'
                    }

                    def "should read main constant value from main module"() {
                        expect:
                            MainConstantValues.MODULE == 'main'
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

                    def "should read constant value from test module"() {
                        expect:
                            ConstantValues.MODULE == 'test'
                    }
                }
                """,
            ).withFile("src/main/resources/a.txt", "main-a")
            .withFile("src/main/resources/b.txt", "main-b")
            .withFile("src/main/resources/c.txt", "main-c")
            .withFile("src/test/resources/b.txt", "test-b")
            .withFile("src/test/resources/c.txt", "test-c")
            .withFile("src/integrationTest/resources/c.txt", "integration-c")
            .build()

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = [GRADLE_MAX_SUPPORTED_VERSION, GRADLE_MIN_SUPPORTED_VERSION])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        // when
        val result = runGradle(project, listOf("check"), gradleVersion)
        // then
        assertThat(result.task(":test")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(SUCCESS)
    }
}
