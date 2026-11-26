plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.todo"
}

dependencies {
    implementation(projects.core.database)
}
