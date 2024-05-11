plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

plugin("java")

fun plugin(name: String) {
  val prefixedId = "template.$name-conventions"
  gradlePlugin.plugins.register(name) {
    id = prefixedId
    implementationClass = "${name.replaceFirstChar(Char::uppercase)}Conventions"
  }
}