import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalArgumentException("libs.versions.toml에 '$alias' 라이브러리가 없습니다") }

internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).orElseThrow { IllegalArgumentException("libs.versions.toml에 '$alias' 버전이 없습니다") }
        .requiredVersion.toInt()

internal val DeskJavaVersion = JavaVersion.VERSION_17

internal fun Project.configureAndroidApplication(extension: ApplicationExtension) = with(extension) {
    compileSdk = libs.intVersion("compileSdk")
    compileSdkMinor = libs.intVersion("compileSdkMinor")
    defaultConfig {
        minSdk = libs.intVersion("minSdk")
        targetSdk = libs.intVersion("targetSdk")
    }
    compileOptions {
        sourceCompatibility = DeskJavaVersion
        targetCompatibility = DeskJavaVersion
    }
}

internal fun Project.configureAndroidLibrary(extension: LibraryExtension) = with(extension) {
    compileSdk = libs.intVersion("compileSdk")
    compileSdkMinor = libs.intVersion("compileSdkMinor")
    defaultConfig {
        minSdk = libs.intVersion("minSdk")
    }
    compileOptions {
        sourceCompatibility = DeskJavaVersion
        targetCompatibility = DeskJavaVersion
    }
}
