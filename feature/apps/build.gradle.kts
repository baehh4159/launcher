plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.apps"
}

dependencies {
    implementation(projects.core.datastore)
    implementation(libs.androidx.core.ktx)
}
