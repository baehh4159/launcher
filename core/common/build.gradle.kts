plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.hilt)
}

android {
    namespace = "com.desklauncher.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.android)
}
