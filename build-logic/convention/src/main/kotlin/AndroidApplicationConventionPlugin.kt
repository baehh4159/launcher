import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** `app` 모듈 공통 설정. Kotlin은 AGP 9 내장 Kotlin을 사용하므로 kotlin-android 플러그인을 적용하지 않는다. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        extensions.configure<ApplicationExtension> { configureAndroidApplication(this) }
        dependencies {
            add("testImplementation", libs.lib("junit4"))
        }
    }
}
