// 예시 플러그인: 물 마시기 기록. 새 플러그인은 scripts/new-plugin.sh로 이 구조를 복제해 만든다.
plugins {
    alias(libs.plugins.desk.plugin)
}

android {
    namespace = "com.desklauncher.plugin.water"
}
