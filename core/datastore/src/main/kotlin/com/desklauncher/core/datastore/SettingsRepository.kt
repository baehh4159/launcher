package com.desklauncher.core.datastore

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<DeskSettings>
    suspend fun setHaptics(value: HapticSetting)
    suspend fun setStripLayout(value: StripLayout)
    suspend fun setScrubHintDone()
}
