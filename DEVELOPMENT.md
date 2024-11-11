# Development

This is a development focused supplement
to [CONTRIBUTING.md](https://github.com/coditory/.github/blob/main/CONTRIBUTING.md).

## Pre commit hook (optional)

Installing pre-commit hook is optional but can save you some headache when pushing unformatted code.

Installing git pre-commit hook that formats code with [Ktlint](https://pinterest.github.io/ktlint):

```sh
cp scripts/git/pre-commit .git/hooks/pre-commit
```

## Commit messages

Before writing a commit message read [this article](https://chris.beams.io/posts/git-commit/).

## Build

Before pushing any changes make sure project builds without errors with:

```
./gradlew build
```

## Code conventions

This repository follows the [Kotlin coding conventions](https://kotlinlang.org/docs/reference/coding-conventions.html).
That are enforced by ktlint and [.editorconfig](../.editorconfig).

You can check style with:

```
./gradlew ktlintCheck
```

## Unit tests and coverage

Use [JUnit 5](https://junit.org/junit5/docs/current/user-guide/) for testing.

Uou can check coverage in `build/reports/kover/` after running:

```
./gradlew test coverage
```

## Validate changes locally

Before submitting a pull request test your changes on a local project.
There are few ways for testing locally a gradle plugin:

**Publish plugin to the local maven repository**

- Publish plugin to your local maven repository (`$HOME/.m2`) with:
  ```sh
  ./gradlew publishToMavenLocal -Pversion="<SOME_VERSION>" && ls -la ~/.m2/repository/com/coditory/gradle/integration-test-plugin
  ```
- Add section to `settings.gradle.kts`:
  ```kt
  // Instruct a sample project to use maven local to find the plugin
  pluginManagement {
    repositories {
      mavenLocal()
      gradlePluginPortal()
    }
  }
  ```
- Add dependency:
  ```kt
  plugins {
    id("com.coditory.integration-test") version "<SOME_VERSION>"
  }
  ```

**Import plugin jar**
Add plugin jar to the sample project (that uses the tested plugin):

```kt
buildscript {
  dependencies {
    classpath(files("<PLUGIN_PROJECT_PATH>/build/libs/integration-test-plugin.jar"))
  }
}

apply(plugin = "com.coditory.integration-test")
```

## Validating plugin module metadata

The easiest way to validate plugin's module metadata is to publish the plugin to a dummy local repository.

Add to `build.gradle.kts`:

```
publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("./local-plugin-repository")
        }
    }
}
```

...and publish the plugin with:

```
./gradlew publish -Pversion=0.0.1
```
