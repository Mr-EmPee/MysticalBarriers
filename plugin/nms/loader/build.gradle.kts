plugins {
    id("template.java-conventions")
    id("io.freefair.lombok") version "8.4"
}

dependencies {
    implementation(project(":plugin:nms:api"))
    implementation(project(":plugin:nms:v1_19"))
}