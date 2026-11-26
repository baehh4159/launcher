plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.desk"
}

dependencies {
    implementation(projects.core.datastore)
}
