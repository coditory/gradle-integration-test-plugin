plugins {
    kotlin("jvm") version "2.0.21"
    id("com.coditory.integration-test") version "2.0.2"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
}

group = "com.coditory.gradle"

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

ktlint {
    version = "1.4.0"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        setExceptionFormat("full")
    }
}

tasks.register("coverage") {
    dependsOn("koverXmlReport", "koverHtmlReport", "koverLog")
}

// Marking new version (incrementPatch [default], incrementMinor, incrementMajor)
// ./gradlew markNextVersion -Prelease.incrementer=incrementMinor
// Releasing the plugin:
// ./gradlew release && ./gradlew publishPlugins
gradlePlugin {
    website = "https://github.com/coditory/gradle-integration-test-plugin"
    vcsUrl = "https://github.com/coditory/gradle-integration-test-plugin"
    plugins {
        create("integrationTestPlugin") {
            id = "com.coditory.integration-test"
            implementationClass = "com.coditory.gradle.integration.IntegrationTestPlugin"
            displayName = "Integration test plugin"
            description = "Gradle Plugin for integration tests"
            tags = listOf("test", "integration", "integration-test", "java-integration-test")
        }
    }
}

// Prints project version.
// Usage: ./gradlew version --quiet
tasks.register("version") {
    doLast {
        println(project.version)
    }
}
