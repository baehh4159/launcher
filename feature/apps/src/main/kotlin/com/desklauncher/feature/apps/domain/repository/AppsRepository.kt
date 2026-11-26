package com.desklauncher.feature.apps.domain.repository

import com.desklauncher.feature.apps.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    /** 설치/삭제/업데이트 시 다시 방출한다. */
    val apps: Flow<List<InstalledApp>>

    /** 실행에 실패하면 false (앱이 방금 지워졌거나 비활성화됨). */
    fun launch(app: InstalledApp): Boolean
}
