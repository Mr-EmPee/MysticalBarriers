plugins {
    id("template.java-conventions")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    implementation(project(":plugin:core"))
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))