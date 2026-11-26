plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.hilt)
    alias(libs.plugins.desk.room)
}

android {
    namespace = "com.desklauncher.core.database"
}

dependencies {
    api(libs.androidx.room.runtime)
    api(libs.kotlinx.coroutines.android)
}
