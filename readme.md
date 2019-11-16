# Integration Test Gradle Plugin

[![Join the chat at https://gitter.im/coditory/gradle-integration-test-plugin](https://badges.gitter.im/coditory/gradle-integration-test-plugin.svg)](https://gitter.im/coditory/gradle-integration-test-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/coditory/gradle-integration-test-plugin.svg?branch=master)](https://travis-ci.org/coditory/gradle-integration-test-plugin)
[![Coverage Status](https://coveralls.io/repos/github/coditory/gradle-integration-test-plugin/badge.svg)](https://coveralls.io/github/coditory/gradle-integration-test-plugin)
[![Gradle Plugin Portal](https://img.shields.io/badge/Plugin_Portal-v1.0.6-green.svg)](https://plugins.gradle.org/plugin/com.coditory.integration-test)

**Zero configuration**, **single responsibility** integration test gradle plugin.

- Adds `integrationTest` task that execute tests under `src/integration/*`.
- Makes `check` task depend on `integrationTest`.
- Makes integration classpath extend test and main classpaths (it this order).
- If `jacoco` plugin is enabled adds `jacocoIntegrationTestReport` and `jacocoIntegrationTestCoverageVerification` tasks.
- Tested on gradle version >=4.9 and 6.0

## Enabling the plugin

Add to your `build.gradle`:

```
plugins {
  id 'com.coditory.integration-test' version '1.0.6'
}
```
