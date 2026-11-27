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

rootProject.name = "DeskLauncher"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// core
include(":core:common")
include(":core:designsystem")
include(":core:tool-api")
include(":core:tool-registry")
include(":core:database")
include(":core:datastore")
include(":core:storage")
include(":core:testing")

// feature — 도구 모듈은 각 기능 작업을 시작할 때 추가한다 (AGENTS.md §6)
include(":feature:desk")
include(":feature:apps")
include(":feature:store")
include(":feature:settings")

// 책상 도구
include(":feature:todo")
include(":feature:memo")
include(":feature:calendar")
include(":feature:diary")
include(":feature:scanner")
include(":feature:calculator")
include(":feature:doodle")
include(":feature:alarm")
include(":feature:files")

// 사용자 플러그인 — plugins/<이름>/build.gradle.kts가 있으면 자동으로 포함된다 (SHELL-14, docs/PLUGIN_GUIDE.md).
// app이 같은 목록을 자동 의존하므로 폴더를 넣고 빼는 것만으로 붙이고 뗄 수 있다.
file("plugins").listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
    ?.sortedBy { it.name }
    ?.forEach { include(":plugins:${it.name}") }
