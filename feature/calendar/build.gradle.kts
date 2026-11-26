plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.calendar"
}

dependencies {
    implementation(projects.core.database)
}
