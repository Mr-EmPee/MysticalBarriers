plugins {
    id("template.java-conventions")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    implementation(project(":plugin:versions:common"))
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))