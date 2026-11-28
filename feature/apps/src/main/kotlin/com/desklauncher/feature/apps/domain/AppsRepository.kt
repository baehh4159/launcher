package com.desklauncher.feature.apps.domain

import com.desklauncher.feature.apps.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    /** 설치·삭제·변경 시 다시 내보낸다 */
    val apps: Flow<List<InstalledApp>>

    /** @return 실행했으면 true */
    fun launch(app: InstalledApp): Boolean
}
