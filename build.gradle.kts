import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig

plugins {
  kotlin("jvm") version "1.3.11"
  id("jacoco")
  id("pl.allegro.tech.build.axion-release") version "1.10.0"
  id("com.github.kt3k.coveralls") version "2.8.2"
  id("com.gradle.plugin-publish") version "0.10.0"
  id("java-gradle-plugin")
  id("maven-publish")
}

repositories {
  jcenter()
}

dependencies {
  implementation(gradleApi())
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.3.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}

scmVersion {
  versionCreator("versionWithBranch")
  tag.prefix = project.name
  hooks = HooksConfig().also {
    it.pre("fileUpdate", mapOf(
      "files" to listOf("readme.md") as Any,
      "pattern" to KotlinClosure2<String, HookContext, String>({ v, _ -> v }),
      "replacement" to KotlinClosure2<String, HookContext, String>({ v, _ -> v })
    ))
    it.pre("commit")
  }
}

group = "com.coditory.gradle"
version = scmVersion.version

tasks {
  withType<Test> {
    testLogging {
      events("passed", "failed", "skipped")
      setExceptionFormat("full")
    }
  }
  withType<Test> {
    useJUnitPlatform()
  }
}

gradlePlugin {
  plugins {
    create("integrationTestPlugin") {
      id = "com.coditory.integration-test"
      implementationClass = "com.coditory.gradle.integration.IntegrationTestPlugin"
    }
  }
}

pluginBundle {
  website = "https://github.com/coditory/gradle-integration-test-plugin"
  vcsUrl = "https://github.com/coditory/gradle-integration-test-plugin"
  description = "Integration test plugin"
  tags = listOf("testing", "test", "integration test")

  (plugins) {
    "integrationTestPlugin" {
      displayName = "Integration test plugin"
    }
  }
}
