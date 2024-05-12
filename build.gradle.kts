import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder

plugins {
  id("template.java-conventions")

  id("io.github.goooler.shadow") version "8.1.7"
  id("xyz.jpenilla.run-paper") version "2.3.0"
  id("io.freefair.lombok") version "8.4"
  id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

group = "io.github.mr-empee.mysticalbarriers"
version = findProperty("tag") ?: "2.1.1-SNAPSHOT"

dependencies {
  implementation(project(":plugin:core"))
}

bukkit {
  val bootstrap = "core.MysticalBarriers"
  main = if (isRelease()) "$group.$bootstrap" else bootstrap

  softDepend = listOf("Multiverse-Core", "MultiWorld", "LuckPerms")
  depend = listOf("ProtocolLib")
  load = PluginLoadOrder.POSTWORLD
  apiVersion = "1.20"
}

tasks.runServer {
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