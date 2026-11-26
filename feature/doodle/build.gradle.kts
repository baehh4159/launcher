plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.doodle"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.storage)
    implementation(libs.androidx.core.ktx)
}
