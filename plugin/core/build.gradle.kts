plugins {
    id("template.java-conventions")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    implementation(project(":plugin:versions:common"))
    implementation(project(":plugin:versions:protocolib"))
    implementation(project(":plugin:versions:spigot:19"))

    implementation(project(":plugin:utils"))

    implementation("io.github.mr-empee:lightwire:0.0.5")

    implementation("io.github.mr-empee.command-forge:bukkit:0.0.1") {
        exclude("com.mojang")
    }

    implementation("io.github.mr-empee:easy-gui:0.0.3")
    implementation("io.github.mr-empee:item-builder:0.0.2")

    implementation("org.dizitart:nitrite-mvstore-adapter:4.2.2") {
        exclude("org.slf4j", "slf4j-api")
    }
    
    implementation("org.dizitart:nitrite:4.2.2") {
        exclude("org.slf4j", "slf4j-api")
    }

    implementation("com.github.cryptomorin:XSeries:9.7.0")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))