package com.desklauncher.feature.apps.domain.model

/** 설치된 실행 가능 앱 (현재 사용자). [key] = ComponentName.flattenToString() */
data class InstalledApp(
    val key: String,
    val packageName: String,
    val label: String,
)

/** 초성/알파벳/# 묶음 */
data class AppSection(val key: String, val apps: List<InstalledApp>)
