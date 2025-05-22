plugins {
    id("template.java-conventions")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))