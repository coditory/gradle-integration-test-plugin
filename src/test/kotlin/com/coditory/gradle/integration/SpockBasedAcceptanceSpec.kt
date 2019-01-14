package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.SampleProject.createProjectFile
import com.coditory.gradle.integration.base.SampleProject.creteBuildGradle
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpockBasedAcceptanceSpec {
  private val projectDir = createTempDir()

  @BeforeEach
  fun setupProject() {
    creteBuildGradle(projectDir, """
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
    """)
    createProjectFile(projectDir, "src/integration/groovy/TestIntgSpec.groovy", """
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
    """)
    createProjectFile(projectDir, "src/test/groovy/TestUnitSpec.groovy", """
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
    """)
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

  @Test
  fun `should run unit tests and integration tests on check command`() {
    val result = GradleRunner.create()
      .withProjectDir(projectDir)
      .withArguments("check", "--debug")
      .withPluginClasspath()
      .forwardOutput()
      .build()
    assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(result.task(":integrationTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }
}
