package com.desklauncher.core.datastore

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<DeskSettings>
    suspend fun setHaptic(value: HapticPreference)
    suspend fun setGripPosition(value: GripPosition)
    suspend fun setTheme(value: ThemePreference)
    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setAppDrawerAutoFocusSearch(value: Boolean)
}
