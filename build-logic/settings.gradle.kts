pluginManagement {
    repositories {
        // kotlin-dsl 플러그인만 포털에서 받는다.
        gradlePluginPortal {
            content { includeGroupByRegex("org\\.gradle.*") }
        }
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
