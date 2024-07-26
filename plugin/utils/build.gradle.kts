plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.15.2-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.12")

    api("net.kyori:adventure-platform-bukkit:4.3.3")
    api("net.kyori:adventure-text-minimessage:4.17.0")
    api("net.kyori:adventure-text-serializer-legacy:4.17.0")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))