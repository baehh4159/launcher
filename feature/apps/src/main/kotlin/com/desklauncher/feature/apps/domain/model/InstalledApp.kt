package com.desklauncher.feature.apps.domain.model

/** 앱 서랍에 보이는 실행 가능한 앱(런처 액티비티) 하나 + 사용자 설정(app_pref). */
data class InstalledApp(
    /** `패키지/액티비티` — PRD §8.3 app_pref.componentKey와 같은 형식 */
    val componentKey: String,
    val packageName: String,
    val label: String,
    /** 시스템 앱은 제거할 수 없다 */
    val isSystemApp: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
)
