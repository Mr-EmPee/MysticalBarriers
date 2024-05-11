plugins {
    id("template.java-conventions")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.15.2-R0.1-SNAPSHOT")
    compileOnly("org.slf4j:slf4j-api:2.0.12")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))