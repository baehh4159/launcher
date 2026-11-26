package com.desklauncher.feature.apps.domain.repository

import com.desklauncher.feature.apps.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    /** 설치/삭제/업데이트 또는 즐겨찾기·숨김 변경 시 다시 방출한다. */
    val apps: Flow<List<InstalledApp>>

    /** 실행에 실패하면 false (앱이 방금 지워졌거나 비활성화됨). */
    fun launch(app: InstalledApp): Boolean

    suspend fun setFavorite(app: InstalledApp, favorite: Boolean)

    suspend fun setHidden(app: InstalledApp, hidden: Boolean)

    /** 시스템 '앱 정보' 화면 */
    fun openAppInfo(app: InstalledApp)

    /** 시스템 제거 확인 대화상자 */
    fun requestUninstall(app: InstalledApp)
}
