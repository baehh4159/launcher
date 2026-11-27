// 책상 도구(플러그인) SDK: 계약(DeskTool·ToolRegistry·DeskToolHost·PluginStorage)과
// 플러그인이 바로 쓰는 작은 UI 도우미(DeskToolScaffold)만 둔다. 데이터·저장 구현은 core:tool-registry.
plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.desklauncher.core.toolapi"
}

dependencies {
    api(projects.core.designsystem)
    api(libs.androidx.navigation.compose)
    api(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
