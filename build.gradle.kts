import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    id("jacoco")
    id("com.github.kt3k.coveralls") version "2.12.0"
    id("com.gradle.plugin-publish") version "0.14.0"
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.41.0")
}

dependencies {
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

group = "com.coditory.gradle"

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        setExceptionFormat("full")
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}

coveralls {
    sourceDirs = listOf("src/main/kotlin")
}

gradlePlugin {
    plugins {
        create("integrationTestPlugin") {
            id = "com.coditory.integration-test"
            implementationClass = "com.coditory.gradle.integration.IntegrationTestPlugin"
        }
    }
}

// Marking new version (incrementPatch [default], incrementMinor, incrementMajor)
// ./gradlew markNextVersion -Prelease.incrementer=incrementMinor
// Releasing the plugin:
// ./gradlew release && ./gradlew publishPlugins
pluginBundle {
    website = "https://github.com/coditory/gradle-integration-test-plugin"
    vcsUrl = "https://github.com/coditory/gradle-integration-test-plugin"
    description = "Gradle Plugin for integration tests"
    tags = listOf("test", "integration", "integration-test", "java-integration-test")

    (plugins) {
        "integrationTestPlugin" {
            displayName = "Integration test plugin"
        }
    }
}
