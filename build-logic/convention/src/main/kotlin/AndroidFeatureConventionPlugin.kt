import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * feature:* 모듈 공통 설정. feature 모듈은 다른 feature 모듈에 의존하지 않는다 (AGENTS.md §4.1).
 * 여러 feature를 잇는 일은 app이 콜백으로 조립한다.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("desk.android.library")
        pluginManager.apply("desk.android.compose")
        pluginManager.apply("desk.hilt")
        dependencies {
            add("implementation", project(":core:common"))
            add("implementation", project(":core:designsystem"))

            add("implementation", libs.lib("androidx-compose-material3"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-hilt-lifecycle-viewmodel-compose"))

            add("testImplementation", project(":core:testing"))
        }
    }
}
