# Integration Test Gradle Plugin

[![Join the chat at https://gitter.im/coditory/gradle-integration-test-plugin](https://badges.gitter.im/coditory/gradle-integration-test-plugin.svg)](https://gitter.im/coditory/gradle-integration-test-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/coditory/gradle-integration-test-plugin.svg?branch=master)](https://travis-ci.org/coditory/gradle-integration-test-plugin)
[![Coverage Status](https://coveralls.io/repos/github/coditory/gradle-integration-test-plugin/badge.svg)](https://coveralls.io/github/coditory/gradle-integration-test-plugin)
[![Gradle Plugin Portal](https://img.shields.io/badge/Plugin_Portal-v1.0.10-green.svg)](https://plugins.gradle.org/plugin/com.coditory.integration-test)

**Zero configuration**, **single responsibility** gradle plugin for integration tests.

- Adds `integrationTest` task that executes tests under `src/integration/*`.
- Adds `testAll` task that executes tests under `src/test/*` and `src/integration/*`.
- Handles flags runtime parameters to skip tests: `skipTest`, `skipIntegrationTest`, `skipTestAll`.
- Makes integration classpath extend test classpath and main classpath (it this order).
- When `jacoco` plugin is enabled adds `jacocoIntegrationTestReport` and `jacocoIntegrationTestCoverageVerification` tasks.
- Tested on java 11 and gradle versions: 4.9, 6.2

## Enabling the plugin

Add to your `build.gradle`:

```
plugins {
  id 'com.coditory.integration-test' version '1.0.10'
}
```

## Usage

Running tests:
```
# Runs tests from /src/test
./gradlew test

# Runs tests /src/integration
./gradlew integrationTest

# Runs all tests (/src/test and /src/integration)
./gradlew testAll
```

Skipping tests:
```
# Skip tests from /src/test
./gradlew clean build -PskipTest

# Skip tests from /src/integration
./gradlew clean build -PskipIntegrationTest

# Skip tests from all tests (/src/test and /src/integration)
./gradlew clean build -PskipTestAll
```
