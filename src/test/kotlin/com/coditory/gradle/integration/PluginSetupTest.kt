package com.coditory.gradle.integration

import com.coditory.gradle.integration.base.SampleProject
import com.coditory.gradle.integration.base.SampleProject.createProject
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.junit.jupiter.api.Test
import org.gradle.api.tasks.testing.Test as TestTask

class PluginSetupTest {
  private val project: Project = createProject(JavaPlugin::class, IntegrationTestPlugin::class)

  @Test
  fun `should register plugin`() {
    assertThat(project.plugins.getPlugin(IntegrationTestPlugin.PLUGIN_ID))
        .isInstanceOf(IntegrationTestPlugin::class.java)
  }

  @Test
  fun `should configure integration source sets`() {
    val sourceSet = getSourceSet(IntegrationTestPlugin.INTEGRATION_CONFIG_PREFIX)
    assertThat(sourceSet).isNotNull
    assertThat(sourceSet.output.classesDirs.asPath).isEqualTo(toBuildPath("classes/java/integration"))
    assertThat(sourceSet.output.resourcesDir.toString()).isEqualTo(toBuildPath("resources/integration"))
    assertThat(sourceSet.runtimeClasspath.asPath)
        .isEqualTo(toBuildPath(
            "classes/java/integration",
            "resources/integration",
            "classes/java/test",
            "resources/test",
            "classes/java/main",
            "resources/main"
        ))
  }

  @Test
  fun `should configure integrationTest task`() {
    val integrationSourceSet = getSourceSet(IntegrationTestPlugin.INTEGRATION_CONFIG_PREFIX)
    val task = getTestTask()
    assertThat(task.testClassesDirs).isNotNull
    assertThat(task.description).isNotEmpty()
    assertThat(task.group).isEqualTo(LifecycleBasePlugin.VERIFICATION_GROUP)
    assertThat(task.testClassesDirs).isEqualTo(integrationSourceSet.output.classesDirs)
    assertThat(task.classpath).isEqualTo(integrationSourceSet.runtimeClasspath)
  }

  @Test
  fun `should add groovy paths to source sets when groovy plugin is enabled`() {
    val project = SampleProject.createProject(GroovyPlugin::class, IntegrationTestPlugin::class)
    assertThat(getSourceSet(project).runtimeClasspath.asPath)
        .isEqualTo(toBuildPath(project,
            "classes/java/integration",
            "classes/groovy/integration",
            "resources/integration",
            "classes/java/test",
            "classes/groovy/test",
            "resources/test",
            "classes/java/main",
            "classes/groovy/main",
            "resources/main"
        ))
  }

  private fun getTestTask(name: String = IntegrationTestPlugin.INTEGRATION_TEST_TASK_NAME): TestTask {
    return project.tasks.getByName(name) as TestTask
  }

  private fun toBuildPath(vararg paths: String): String {
    return toBuildPath(project, *paths)
  }

  private fun toBuildPath(project: Project, vararg paths: String): String {
    return paths.joinToString(":") { "${project.buildDir}/$it" }
  }

  private fun getSourceSet(name: String = IntegrationTestPlugin.INTEGRATION_CONFIG_PREFIX): SourceSet {
    return getSourceSet(project, name)
  }

  private fun getSourceSet(project: Project, name: String = IntegrationTestPlugin.INTEGRATION_CONFIG_PREFIX): SourceSet {
    return project.convention.getPlugin(JavaPluginConvention::class.java)
        .sourceSets.getByName(name)
  }
}
