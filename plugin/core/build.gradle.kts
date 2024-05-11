plugins {
    id("template.java-conventions")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")

    implementation(project(":plugin:nms"))
    implementation(project(":plugin:utils"))

    implementation("io.github.mr-empee:lightwire:0.0.1-SNAPSHOT")
    implementation("io.github.mr-empee:colonel:0.0.3-SNAPSHOT")
    implementation("io.github.mr-empee:easy-gui:0.0.1-SNAPSHOT")
    implementation("io.github.mr-empee:item-builder:0.0.1-SNAPSHOT")

    implementation(platform("org.dizitart:nitrite-bom:4.2.2"))
    implementation("org.dizitart:nitrite-mvstore-adapter")
    implementation("org.dizitart:nitrite")

    implementation("com.github.cryptomorin:XSeries:9.7.0")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))