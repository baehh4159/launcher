// DeskTool 레지스트리 구현. 셸(feature:desk)과 도구함(feature:store)이 함께 쓰므로
// feature 간 의존을 피하기 위해 core 모듈로 둔다.
plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.hilt)
}

android {
    namespace = "com.desklauncher.core.toolregistry"
}

dependencies {
    api(projects.core.toolApi)
    implementation(projects.core.database)
}
