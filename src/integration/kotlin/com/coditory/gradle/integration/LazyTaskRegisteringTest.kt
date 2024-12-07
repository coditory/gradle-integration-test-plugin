package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.TestProjectBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.Test

class LazyTaskRegisteringTest {
    companion object {
        const val TEST_CONFIG_LOG = "Long running configuration..."

        @AutoClose
        private val project = TestProjectBuilder
            .project(LazyTaskRegisteringTest::class.simpleName!!)
            .withBuildGradleKts(
                """
                    plugins {
                        id("jacoco")
                        id("com.coditory.integration-test")
                    }

                    repositories {
                        mavenCentral()
                    }

                    dependencies {
                        testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
                        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
                    }

                    tasks.withType<Test>().configureEach {
                        println("$TEST_CONFIG_LOG")
                    }
                    """,
            )
            .build()
    }

    @AfterEach
    fun cleanProject() {
        project.clean()
    }

    @Test
    fun `should register test tasks in a lazy manner`() {
        // when
        val result = project.runGradle(listOf("clean"))
        // then
        assertThat(result.output).doesNotContain(TEST_CONFIG_LOG)
    }
}
