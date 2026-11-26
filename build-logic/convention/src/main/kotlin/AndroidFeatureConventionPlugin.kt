import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * feature:* 모듈 공통 설정.
 * feature 모듈은 다른 feature 모듈에 의존하지 않는다 (AGENTS.md §4.1).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("desk.android.library")
        pluginManager.apply("desk.android.compose")
        pluginManager.apply("desk.hilt")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        dependencies {
            add("implementation", project(":core:common"))
            add("implementation", project(":core:designsystem"))
            add("implementation", project(":core:tool-api"))

            add("implementation", libs.lib("androidx-compose-material3"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-navigation-compose"))
            add("implementation", libs.lib("androidx-hilt-navigation-compose"))
            add("implementation", libs.lib("kotlinx-serialization-json"))
        }
    }
}
