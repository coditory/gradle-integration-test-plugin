# Integration Test Gradle Plugin

[![Build](https://github.com/coditory/gradle-integration-test-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/gradle-integration-test-plugin/actions/workflows/build.yml)
[![Coverage](https://codecov.io/gh/coditory/gradle-integration-test-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/coditory/gradle-integration-test-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.coditory.integration-test)](https://plugins.gradle.org/plugin/com.coditory.integration-test)

> Single line in build.gradle.kts to enable integration tests in JVM projects

**Zero configuration**, **single responsibility** gradle plugin for integration tests.

- Adds `integrationTest` task that executes tests under `src/integration/*`.
- Adds `testAll` task that executes tests under `src/test/*` and `src/integration/*`.
- Handles flags parameters to skip tests `skipTest`, `skipIntegrationTest`, `skipUnitTest`.
- Makes integration classpath extend test classpath and main classpath (in this order).
- Makes sure IntelliJ idea treats `src/integration/*` as test sources.
- Exposes kotlin internal scope (from main and test module) to integration tests.
- Integrates with test coverage tools like [Jacoco](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
  and [Kover](https://github.com/Kotlin/kotlinx-kover).
- Integrates with test frameworks like [JUnit5](https://junit.org/junit5/), [Spock](https://spockframework.org/) and
  [Kotest](https://kotest.io/).
- Compatible with [gradle configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html)
  and [lazy task configuration](https://docs.gradle.org/current/userguide/lazy_configuration.html).

## Using the plugin

Update `build.gradle.kts`

```gradle
plugins {
  id("com.coditory.integration-test") version "2.2.5"
}

dependencies {
  integrationImplementation(...)
}
```

Add integration tests under `src/integration`. That's it!

There are more details below but the rest is quite obvious as it suppose to be.

### Sample usages with different test frameworks

See a [project](https://github.com/coditory/gradle-integration-test-plugin-sample) with all the examples.

<details><summary>Java + JUnit5 (<a href="https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/java-junit5">project</a>)</summary>
<p>

```gradle
// build.gradle.kts
plugins {
    id("java")
    id("com.coditory.integration-test") version "2.2.5"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.11.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

</p>
</details>
<details><summary>Groovy + Spock (<a href="https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/groovy-spock">project</a>)</summary>
<p>

```gradle
// build.gradle
plugins {
    id "groovy"
    id "com.coditory.integration-test" version "2.2.5"
}

dependencies {
    testCompile "org.spockframework:spock-core:2.4-M4-groovy-4.0"
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
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.21"
    id("com.coditory.integration-test") version "2.2.5"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
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
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.0.21"
    id("com.coditory.integration-test") version "2.2.5"
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
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
./gradlew clean build -PskipTest

# Skip tests from /src/test
./gradlew clean build -x test
# ...or skipUnitTests=true/false
./gradlew clean build -PskipUnitTest

# Skip tests from /src/integration
./gradlew clean build -x integrationTest
# ...or skipIntegrationTests=true/false
./gradlew clean build -PskipIntegrationTest
```

[Test filtering](https://docs.gradle.org/current/userguide/java_testing.html#test_filtering) is supported as well:

```sh
./gradlew iT --tests com.coditory.SampleTest.shouldWork
```

## The no-plugin alternative

If you're against adding plugins to your build file, simply copy-paste the configuration from:

- [Java + Junit5 (no plugin)](https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/java-junit5-no-plugin/build.gradle)
- [Kotlin + Junit5 (no plugin)](https://github.com/coditory/gradle-integration-test-plugin-sample/tree/master/kotlin-junit5-no-plugin/build.gradle.kts)

...though mind the boilerplate

## Migrating from 1.x.x to 2.x.x

- Skipping flags changed names. Use `skipTests`, `skipUnitTests`, `skipIntegrationTests`
  instead of `skipTest`, `skipUnitTest`, `skipIntegrationTest`.
- Added integration with Jacoco - coverage from integration tests is automatically included in report.
- Integration with JUnit4 is dropped.
