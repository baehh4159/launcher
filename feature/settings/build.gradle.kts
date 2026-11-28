// 설정 (SET-01의 일부를 M0에서 먼저: 촉각(STRIP-03)·서류철 배치 비교(STRIP-04))
plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.settings"
}

dependencies {
    implementation(projects.core.datastore)
}
