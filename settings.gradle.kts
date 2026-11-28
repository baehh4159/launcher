pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DeskNotebook"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// core (AGENTS.md §4.1)
include(":core:common")
include(":core:designsystem")
include(":core:datastore")
include(":core:testing")

// feature — 각 기능 ID 작업을 시작할 때 추가한다
include(":feature:desk")
include(":feature:apps")
