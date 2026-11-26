// 앱 내부 저장소 경로·FileProvider 규약 (PRD §8.5). 파일 경로 문자열을 다른 모듈에 흩지 않기 위함.
plugins {
    alias(libs.plugins.desk.android.library)
    alias(libs.plugins.desk.hilt)
}

android {
    namespace = "com.desklauncher.core.storage"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
