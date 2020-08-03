package com.coditory.gradle.integration.acceptance

import com.coditory.gradle.integration.base.SpecProjectBuilder.Companion.project
import com.coditory.gradle.integration.base.SpecProjectRunner.runGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SpockBasedAcceptanceSpec {
    private val project = project("sample-project")
        .withBuildGradle(
            """
            plugins {
                id 'groovy'
                id 'com.coditory.integration-test'
            }

            repositories {
                jcenter()
            }

            dependencies {
                testCompile "junit:junit:4.12"
                testCompile "org.codehaus.groovy:groovy-all:2.4.13"
                testCompile "org.spockframework:spock-core:1.1-groovy-2.4"
            }
            """
        ).withFile(
            "src/integration/groovy/TestIntgSpec.groovy",
            """
            import spock.lang.Specification

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

                private String readFile(String name) {
                    return getClass().getResource("/" + name).getText()
                }
            }
            """
        ).withFile(
            "src/test/groovy/TestUnitSpec.groovy",
            """
            import spock.lang.Specification

            class TestUnitSpec extends Specification {
                def "should read a.txt from main"() {
                    expect:
                        readFile('a.txt') == 'main-a'
                }

                def "should read b.txt from test"() {
                    expect:
                        readFile('b.txt') == 'test-b'
                }

                private String readFile(String name) {
                    return getClass().getResource("/" + name).getText()
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

    @ParameterizedTest(name = "should run unit tests and integration tests on check command for gradle {0}")
    @ValueSource(strings = ["current", "5.0"])
    fun `should run unit tests and integration tests on check command`(gradleVersion: String?) {
        val result = runGradle(project, listOf("check", "--debug"), gradleVersion)
        assertThat(result.task(":test")?.outcome).isEqualTo(SUCCESS)
        assertThat(result.task(":integrationTest")?.outcome).isEqualTo(SUCCESS)
    }
}
