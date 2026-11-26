plugins {
    alias(libs.plugins.desk.android.application)
    alias(libs.plugins.desk.android.compose)
    alias(libs.plugins.desk.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.desklauncher"

    defaultConfig {
        applicationId = "com.desklauncher"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.toolApi)
    implementation(projects.core.toolRegistry)
    implementation(projects.core.database)
    implementation(projects.core.datastore)

    implementation(projects.feature.desk)
    implementation(projects.feature.apps)
    implementation(projects.feature.store)
    implementation(projects.feature.settings)

    // 책상 도구 (DeskTool @IntoSet)
    implementation(projects.feature.todo)
    implementation(projects.feature.memo)
    implementation(projects.feature.calendar)
    implementation(projects.feature.diary)
    implementation(projects.feature.scanner)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.serialization.json)
}
