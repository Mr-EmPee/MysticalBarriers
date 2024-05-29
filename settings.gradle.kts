rootProject.name = "MysticalBarriers"

includeBuild("./gradle/build-logic")

include(":plugin:utils")
include(":plugin:core")

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

  repositories {
    mavenCentral()

    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
  }
}