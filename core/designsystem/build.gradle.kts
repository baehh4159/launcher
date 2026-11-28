// 디자인 토큰(DeskTheme)·모션(DeskMotion)·촉각(DeskHaptics)·원형 컴포넌트 (docs/DESIGN_LANGUAGE.md)
plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.android.compose)
}

android {
    namespace = "com.desklauncher.core.designsystem"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
