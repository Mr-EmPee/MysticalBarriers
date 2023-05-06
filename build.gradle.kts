import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.gradle.internal.impldep.bsh.commands.dir

plugins {
  id("org.gradle.java-library")
  id("org.gradle.checkstyle")

  id("io.freefair.lombok") version "6.6.3"
  id("com.github.johnrengelman.shadow") version "8.1.0"

  id("io.papermc.paperweight.userdev") version "1.5.2"
  id("xyz.jpenilla.run-paper") version "2.0.1"
  id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
}

group = "ml.empee"
version = "2.0.0"
var basePackage = "ml.empee.mysticalbarriers"

bukkit {
  load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
  main = "${basePackage}.MysticalBarriers"
  apiVersion = "1.13"
  depend = listOf("ProtocolLib")
  authors = listOf("Mr. EmPee")
}

repositories {
  maven("https://repo.dmulloy2.net/repository/public/")
  maven("https://jitpack.io")

  mavenCentral()
}

dependencies {
  paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT")
  compileOnly("org.xerial:sqlite-jdbc:3.34.0")

  // Core depends
  compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
  implementation("com.github.Mr-EmPee:SimpleIoC:1.7.1")

  implementation("me.lucko:commodore:2.2") {
    exclude("com.mojang", "brigadier")
  }

  implementation("cloud.commandframework:cloud-paper:1.8.3")
  implementation("cloud.commandframework:cloud-annotations:1.8.3")

  // Utilities
  implementation("com.github.Mr-EmPee:SimpleHeraut:1.0.1")
  implementation("com.github.Mr-EmPee:ItemBuilder:1.0.0")

  //implementation("io.github.rysefoxx.inventory:RyseInventory-Plugin:1.5.7")
  //implementation("com.j256.ormlite:ormlite-jdbc:6.1")
}

tasks {
  checkstyle {
    toolVersion = "10.10.0"
    configFile = file("$projectDir/checkstyle.xml")
  }

  shadowJar {
    isEnableRelocation = false
    relocationPrefix = "$basePackage.relocations"
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }

  processResources {
    filteringCharset = Charsets.UTF_8.name()
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)
  }
}

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
