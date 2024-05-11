import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import java.nio.file.Files

class JavaConventions : Plugin<Project> {

  override fun apply(target: Project) {
    val plugins = target.plugins
    plugins.apply("java")

    val dependencies = target.dependencies
    dependencies.add("compileOnly", "org.jetbrains:annotations:24.1.0")

    val projectPath = target.projectDir.toPath()
    val testPath = projectPath.resolve("src").resolve("test").resolve("java")
    if (Files.exists(testPath)) {
      applyJunit(target)
    }
  }

  private fun applyJunit(target: Project) {
    val dependencies = target.dependencies
    dependencies.add("testImplementation", dependencies.platform("org.junit:junit-bom:5.9.1"))
    dependencies.add("testImplementation", "org.junit.jupiter:junit-jupiter")

    val tasks = target.tasks
    tasks.withType(Test::class.java) {
      useJUnitPlatform()
    }
  }

}