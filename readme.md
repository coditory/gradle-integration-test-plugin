# Integration Test Gradle Plugin

[![Join the chat at https://gitter.im/coditory/gradle-integration-test-plugin](https://badges.gitter.im/coditory/gradle-integration-test-plugin.svg)](https://gitter.im/coditory/gradle-integration-test-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/coditory/gradle-integration-test-plugin.svg?branch=master)](https://travis-ci.org/coditory/gradle-integration-test-plugin)
[![Coverage Status](https://coveralls.io/repos/github/coditory/gradle-integration-test-plugin/badge.svg)](https://coveralls.io/github/coditory/gradle-integration-test-plugin)
[![Gradle Plugin Portal](https://img.shields.io/badge/Plugin_Portal-v1.1.1-green.svg)](https://plugins.gradle.org/plugin/com.coditory.integration-test)

**Zero configuration**, **single responsibility** gradle plugin for integration tests.

- Adds `integrationTest` task that executes tests under `src/integration/*`.
- Adds `testAll` task that executes tests under `src/test/*` and `src/integration/*`.
- Handles flags runtime parameters to skip tests: `skipTests`, `skipIntegrationTests`, `skipUnitTests`.
- Makes integration classpath extend test classpath and main classpath (it this order).
- When `jacoco` plugin is enabled adds `jacocoIntegrationTestReport` and `jacocoIntegrationTestCoverageVerification` tasks.
- Tested on java 11 and gradle versions: 4.9, 6.2

## Enabling the plugin

Add to your `build.gradle`:

```
plugins {
  id 'com.coditory.integration-test' version '1.1.1'
}
```

## Usage

Running tests:
```
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
```
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
```
./gradlew iT --tests com.coditory.SampleTest.shouldWork
```
