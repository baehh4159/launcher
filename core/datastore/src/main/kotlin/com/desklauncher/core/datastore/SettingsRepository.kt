package com.desklauncher.core.datastore

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<DeskSettings>
    suspend fun setHaptics(value: HapticSetting)
    suspend fun setStripLayout(value: StripLayout)
    /** 훑기로 처음 열면 true, 설정의 '안내 다시 보기'는 false */
    suspend fun setScrubHintDone(done: Boolean)
    suspend fun setPinnedApps(keys: List<String>)
}
