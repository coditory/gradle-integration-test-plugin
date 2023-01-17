# Contributing

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

## Unit tests
We use [JUnit 5](https://junit.org/junit5/docs/current/user-guide/) for testing.
Please use the `Spec.kt` suffix on new test classes.

## Validate changes locally
Before submitting a pull request test your changes on a local project.
There are few ways for testing locally a gradle plugin:

**Publish plugin to the local maven repository**
Publish plugin to local repository with:
```sh
./gradlew publishToMavenLocal
```

...and add section to `settings.gradle.kts` to the sample project (that uses the tested plugin):
```kt
// Instruct a sample project to use maven local to find the plugin
pluginManagement {
 repositories {
     mavenLocal()
     gradlePluginPortal()
 }
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

apply(plugin = "com.coditory.build")
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
