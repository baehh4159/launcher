package com.desklauncher.feature.apps.domain.model

/** 앱 서랍에 보이는 실행 가능한 앱(런처 액티비티) 하나. */
data class InstalledApp(
    /** `패키지/액티비티` — PRD §8.3 app_pref.componentKey와 같은 형식 */
    val componentKey: String,
    val packageName: String,
    val label: String,
)
