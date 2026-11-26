// 서류정리함 + ML Kit 문서 스캐너 (toolId "cabinet", PRD §5.6)
plugins {
    alias(libs.plugins.desk.android.feature)
}

android {
    namespace = "com.desklauncher.feature.scanner"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.storage)
    implementation(libs.mlkit.document.scanner)
    implementation(libs.androidx.core.ktx)
}
