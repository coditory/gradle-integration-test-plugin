import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    id("jacoco")
    id("com.github.kt3k.coveralls") version "2.12.2"
    id("com.gradle.plugin-publish") version "1.2.1"
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.45.2")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

coveralls {
    sourceDirs = listOf("src/main/kotlin")
}

// Marking new version (incrementPatch [default], incrementMinor, incrementMajor)
// ./gradlew markNextVersion -Prelease.incrementer=incrementMinor
// Releasing the plugin:
// ./gradlew release && ./gradlew publishPlugins
gradlePlugin {
    plugins {
        create("integrationTestPlugin") {
            id = "com.coditory.integration-test"
            implementationClass = "com.coditory.gradle.integration.IntegrationTestPlugin"
            displayName = "Integration test plugin"
            description = "Gradle Plugin for integration tests"
        }
    }
}

pluginBundle {
    website = "https://github.com/coditory/gradle-integration-test-plugin"
    vcsUrl = "https://github.com/coditory/gradle-integration-test-plugin"
    tags = listOf("test", "integration", "integration-test", "java-integration-test")
}
