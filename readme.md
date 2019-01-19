# Integration Test Gradle Plugin

[![Join the chat at https://gitter.im/coditory/gradle-integration-test-plugin](https://badges.gitter.im/coditory/gradle-integration-test-plugin.svg)](https://gitter.im/coditory/gradle-integration-test-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/coditory/gradle-integration-test-plugin.svg?branch=master)](https://travis-ci.org/coditory/gradle-integration-test-plugin)
[![Coverage Status](https://coveralls.io/repos/coditory/gradle-integration-test-plugin/badge.svg?branch=development)](https://coveralls.io/r/coditory/gradle-integration-test-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.gradle/gradle-integration-test-plugin/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.coditory.gradle/gradle-integration-test-plugin)

**Zero configuration**, **single responsibility** integration test gradle plugin.

- Adds `integrationTest` task that execute tests under `src/integration/*`.
- Makes `check` task depend on `integrationTest`.
- Makes integration classpath extend main and test classpaths.
- If `jacoco` plugin is enabled adds `jacocoIntegrationTestReport` and `jacocoIntegrationTestCoverageVerification` tasks.

## Enabling the plugin

Add to the `build.gradle`:

```
plugins {
  id 'java'
  id 'com.coditory.integration-test' version '0.1.3'
}
```
