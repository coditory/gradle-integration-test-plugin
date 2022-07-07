# Integration Test Gradle Plugin
[![Build Status](https://github.com/coditory/gradle-integration-test-plugin/workflows/Build/badge.svg?branch=master)](https://github.com/coditory/gradle-integration-test-plugin/actions?query=workflow%3ABuild+branch%3Amaster)
[![Coverage Status](https://coveralls.io/repos/github/coditory/gradle-integration-test-plugin/badge.svg?branch=master)](https://coveralls.io/github/coditory/gradle-integration-test-plugin?branch=master)
[![Gradle Plugin Portal](https://img.shields.io/badge/Plugin_Portal-v1.4.2-green.svg)](https://plugins.gradle.org/plugin/com.coditory.integration-test)
[![Join the chat at https://gitter.im/coditory/gradle-integration-test-plugin](https://badges.gitter.im/coditory/gradle-integration-test-plugin.svg)](https://gitter.im/coditory/gradle-integration-test-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

**Zero configuration**, **single responsibility** gradle plugin for integration tests.

- Adds `integrationTest` task that executes tests under `src/integration/*`.
- Adds `testAll` task that executes tests under `src/test/*` and `src/integration/*`.
- Handles runtime flags parameters to skip tests: `skipTests`, `skipIntegrationTests`, `skipUnitTests`.
- Makes integration classpath extend test classpath and main classpath (in this order).
- Configures `idea` plugin to treat integration source dirs as test dirs (only when `idea` plugin is enabled or there is `.idea` folder in project root directory).
- Exposes internal scope (from main and test module) to integration tests
- Deobfuscates build gradle from boilerplate code

## Enabling the plugin

Add to your `build.gradle`:

```gradle
plugins {
  id "com.coditory.integration-test" version "1.4.2"
}
```

### Sample usages with different test frameworks
See a [project](https://github.com/coditory/gradle-integration-test-plugin-sample) with all the examples.

<details><summary>Java + JUnit4 (<a href="https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/java-junit4">project</a>)</summary>
<p>

```gradle
plugins {
    id "java"
    id "com.coditory.integration-test" version "1.4.2"
}

dependencies {
    testCompile "junit:junit:4.12"
}
```
</p>
</details>
<details><summary>Java + JUnit5 (<a href="https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/java-junit5">project</a>)</summary>
<p>

```gradle
plugins {
    id "java"
    id "com.coditory.integration-test" version "1.4.2"
}

dependencies {
    testImplementation "org.junit.jupiter:junit-jupiter-api:5.7.2"
    testRuntime "org.junit.jupiter:junit-jupiter-engine:5.7.2"
}

tasks.withType(Test) {
    useJUnitPlatform()
}
```
</p>
</details>
<details><summary>Groovy + Spock (<a href="https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/groovy-spock">project</a>)</summary>
<p>

```gradle
plugins {
    id "groovy"
    id "com.coditory.integration-test" version "1.4.2"
}

dependencies {
    testCompile "org.spockframework:spock-core:2.0-groovy-3.0"
}

tasks.withType(Test) {
    useJUnitPlatform()
}
```
</p>
</details>
<details><summary>Kotlin + JUnit5 (<a href="https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/kotlin-junit5">project</a>)</summary>
<p>

```gradle
plugins {
    kotlin("jvm") version "1.7.0"
    id("com.coditory.integration-test") version "1.4.2"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```
</p>
</details>
<details><summary>Kotlin + Kotest (<a href="https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/kotlin-kotest">project</a>)</summary>
<p>

```gradle
plugins {
    kotlin("jvm") version "1.7.0"
    id("com.coditory.integration-test") version "1.4.2"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.3.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```
</p>
</details>

## Usage

Running tests:
```sh
# Runs tests from /src/test
./gradlew test

# Runs tests /src/integration
./gradlew integrationTest
./gradlew iT

# Runs all tests (/src/test and /src/integration)
./gradlew testAll
./gradlew tA
```

Skipping tests:
```sh
# Skip all tests
./gradlew clean build -x test integrationTest
# ...or skipTests=true/false
./gradlew clean build -PskipTests

# Skip tests from /src/test
./gradlew clean build -x test
# ...or skipUnitTests=true/false
./gradlew clean build -PskipUnitTests

# Skip tests from /src/integration
./gradlew clean build -x integrationTest
# ...or skipIntegrationTests=true/false
./gradlew clean build -PskipIntegrationTests
```

[Test filtering](https://docs.gradle.org/current/userguide/java_testing.html#test_filtering) is supported as well:
```sh
./gradlew iT --tests com.coditory.SampleTest.shouldWork
```

Creating a single [Jacoco](https://docs.gradle.org/current/userguide/jacoco_plugin.html) report for unit and integration tests:

```gradle
jacocoTestReport {
    executionData(fileTree(project.buildDir).include("jacoco/*.exec"))
    reports {
        xml.enabled = true
        html.enabled = true
    }
}
```

## The no-plugin alternative

If you're against adding plugins to your build file, simply copy-paste the configuration from:
- [Java + Junit5 (no plugin)](https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/java-junit5-no-plugin)
- [Kotlin + Junit5 (no plugin)](https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/kotlin-junit5-no-plugin)

...though it's not a one-liner, be aware of the obfuscation