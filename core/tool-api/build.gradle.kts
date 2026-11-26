// DeskTool 플러그인 계약만 둔다. 구현 코드 금지 (AGENTS.md §4.1).
plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.android.compose)
}

android {
    namespace = "com.desklauncher.core.toolapi"
}

dependencies {
    api(projects.core.designsystem)
    api(libs.androidx.navigation.compose)
    api(libs.kotlinx.coroutines.android)
}
