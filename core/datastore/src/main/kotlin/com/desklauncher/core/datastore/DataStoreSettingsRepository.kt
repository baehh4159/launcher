package com.desklauncher.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** 열거형은 이름(String)으로 저장한다 — 순서 변경에 안전 (PRD §8.1). 알 수 없는 값은 기본값. */
internal class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<DeskSettings> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs ->
            val defaults = DeskSettings()
            DeskSettings(
                haptic = prefs[Keys.Haptic].toEnum(defaults.haptic),
                gripPosition = prefs[Keys.Grip].toEnum(defaults.gripPosition),
                theme = prefs[Keys.Theme].toEnum(defaults.theme),
                onboardingCompleted = prefs[Keys.OnboardingCompleted] ?: defaults.onboardingCompleted,
                appDrawerAutoFocusSearch = prefs[Keys.DrawerAutoFocus] ?: defaults.appDrawerAutoFocusSearch,
            )
        }
        .distinctUntilChanged()

    override suspend fun setHaptic(value: HapticPreference) = put(Keys.Haptic, value.name)
    override suspend fun setGripPosition(value: GripPosition) = put(Keys.Grip, value.name)
    override suspend fun setTheme(value: ThemePreference) = put(Keys.Theme, value.name)
    override suspend fun setOnboardingCompleted(value: Boolean) = put(Keys.OnboardingCompleted, value)
    override suspend fun setAppDrawerAutoFocusSearch(value: Boolean) = put(Keys.DrawerAutoFocus, value)

    private suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    private inline fun <reified E : Enum<E>> String?.toEnum(default: E): E =
        enumValues<E>().firstOrNull { it.name == this } ?: default

    private object Keys {
        val Haptic = stringPreferencesKey("haptic")
        val Grip = stringPreferencesKey("grip_position")
        val Theme = stringPreferencesKey("theme")
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val DrawerAutoFocus = booleanPreferencesKey("app_drawer_auto_focus_search")
    }
}
