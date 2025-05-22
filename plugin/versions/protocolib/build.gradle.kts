plugins {
    id("template.java-conventions")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

    implementation(project(":plugin:versions:common"))
    implementation(project(":plugin:utils"))
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))