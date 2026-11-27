plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.alarm"
}

dependencies {
    implementation(projects.core.database)
    implementation(libs.androidx.core.ktx)
}
