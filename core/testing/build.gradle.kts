// 테스트 공용 Fake·규칙 (testImplementation으로만 쓴다)
plugins {
    alias(libs.plugins.desk.android.library)
}

android {
    namespace = "com.desklauncher.core.testing"
}

dependencies {
    api(projects.core.datastore)
    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
}
