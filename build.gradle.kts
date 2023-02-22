import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
  id("org.gradle.java-library")
  id("io.freefair.lombok") version "6.6.1"
  id("com.github.johnrengelman.shadow") version "7.1.2"

  id("io.papermc.paperweight.userdev") version "1.5.0"
  id("xyz.jpenilla.run-paper") version "2.0.1"
  id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "ml.empee"
version = "1.0.0-SNAPSHOT"
var basePackage = "ml.empee.mysticalbarriers"

bukkit {
  load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
  main = "${basePackage}.MysticalBarriersPlugin"
  apiVersion = "1.13"
  depend = listOf("ProtocolLib")
  softDepend = listOf("Multiverse-Core", "MultiWorld", "LuckPerms")
  authors = listOf("Mr. EmPee")
}

repositories {
  maven("https://repo.dmulloy2.net/repository/public/")
  maven("https://jitpack.io")
}

dependencies {
  paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT")
  compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

  implementation("com.github.Mr-EmPee:SimpleIoC:1.5.0")
  implementation("com.github.Mr-EmPee:ImperialEdicta:1.0.0")
  implementation("com.github.Mr-EmPee:JsonPersistence:2.1.0")
  implementation("com.github.Mr-EmPee:SimpleLectorem:1.0.0")
  implementation("com.github.Mr-EmPee:SimpleHeraut:1.0.0")
  implementation("com.github.Mr-EmPee:ItemBuilder:1.0.0")
  implementation("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.5.6.2")
}

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  compileJava {
    options.release.set(17)
  }

  shadowJar {
    fun relocate(pkg: String) = relocate(pkg, "${basePackage}.relocations.${pkg}")

    relocate("ml.empee.commandsManager")
    relocate("ml.empee.configurator")
    relocate("ml.empee.ioc")
    relocate("ml.empee.itembuilder")
    relocate("ml.empee.json")
    relocate("ml.empee.notifier")

    relocate("io.github.rysefoxx.inventory")
  }
}
