plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.android.compose)
}

android {
    namespace = "com.desklauncher.core.designsystem"
}

dependencies {
    // 버전 없는 Compose 의존성을 api로 내보내므로 BOM도 api로 전파한다.
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
}
