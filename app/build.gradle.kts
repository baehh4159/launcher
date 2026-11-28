plugins {
    alias(libs.plugins.desk.android.application)
    alias(libs.plugins.desk.android.compose)
    alias(libs.plugins.desk.hilt)
}

android {
    namespace = "com.desklauncher"

    defaultConfig {
        // v1과 같은 패키지 — 폰에 설치된 v1 위에 덮어 설치된다 (PRD §21)
        applicationId = "com.desklauncher"
        versionCode = 2
        versionName = "0.2.0"
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
    implementation(projects.core.datastore)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material3)
}
