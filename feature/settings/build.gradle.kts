plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.settings"
}

dependencies {
    implementation(projects.core.datastore)
}
