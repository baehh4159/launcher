plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.memo"
}

dependencies {
    implementation(projects.core.database)
}
