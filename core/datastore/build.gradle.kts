plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.hilt)
}

android {
    namespace = "com.desklauncher.core.datastore"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.datastore.preferences)
}
