import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder

plugins {
  id("template.java-conventions")

  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("xyz.jpenilla.run-paper") version "2.0.1"
  id("io.freefair.lombok") version "8.4"
  id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

group = "net.jarcloud.template"
version = findProperty("tag") ?: "0.0.1-SNAPSHOT"

dependencies {
  implementation(project(":plugin:core"))
}

bukkit {
  val bootstrap = "plugin.MysticalBarriers"
  main = if (isRelease()) "$group.$bootstrap" else bootstrap

  softDepend = listOf("Multiverse-Core", "MultiWorld", "LuckPerms")
  depend = listOf("ProtocolLib")
  load = PluginLoadOrder.POSTWORLD
  apiVersion = "1.20"
}

tasks.runServer {
  runDirectory.set(file(".run"))
  minecraftVersion("1.20.1")
}

tasks.shadowJar {
  isEnableRelocation = isRelease()
  relocationPrefix = project.group.toString()
}

fun isRelease(): Boolean {
  return !project.version.toString().endsWith("-SNAPSHOT")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))