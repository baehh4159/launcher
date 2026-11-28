// 책상(HOME-*)·서류철(STRIP-*)
plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.desk"
}

dependencies {
    implementation(projects.core.datastore)
}
