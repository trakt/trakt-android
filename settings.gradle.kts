import java.net.URI

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = rootProject.projectDir.resolve("./external/younify").toURI() }
        maven { url = URI("https://jitpack.io") }
    }
}

rootProject.name = "Trakt"
include(":app")
include(":tv")
include(":common")
include(":resources")
