rootProject.name = "MysticalBarriers"

includeBuild("./gradle/build-logic")

include(":plugin:utils")

include(":plugin:nms:api")
include(":plugin:nms:v1_19")
include(":plugin:nms:loader")
include(":plugin:nms")

include(":plugin:core")
include(":plugin:api")

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

  repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
  }
}