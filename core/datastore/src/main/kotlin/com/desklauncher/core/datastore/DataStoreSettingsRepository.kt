package com.desklauncher.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<DeskSettings> = dataStore.data
        .map { prefs ->
            DeskSettings(
                haptics = prefs[Keys.Haptics].toEnum(HapticSetting.Full),
                stripLayout = prefs[Keys.StripLayout].toEnum(StripLayout.Split),
                scrubHintDone = prefs[Keys.ScrubHintDone] ?: false,
                pinnedApps = prefs[Keys.PinnedApps]?.split(ListSeparator)?.filter(String::isNotEmpty).orEmpty(),
            )
        }
        .distinctUntilChanged()

    override suspend fun setHaptics(value: HapticSetting) {
        dataStore.edit { it[Keys.Haptics] = value.name }
    }

    override suspend fun setStripLayout(value: StripLayout) {
        dataStore.edit { it[Keys.StripLayout] = value.name }
    }

    override suspend fun setScrubHintDone(done: Boolean) {
        dataStore.edit { it[Keys.ScrubHintDone] = done }
    }

    override suspend fun setPinnedApps(keys: List<String>) {
        dataStore.edit { it[Keys.PinnedApps] = keys.joinToString(ListSeparator) }
    }

    private object Keys {
        val Haptics = stringPreferencesKey("haptics")
        val StripLayout = stringPreferencesKey("strip_layout")
        val ScrubHintDone = booleanPreferencesKey("scrub_hint_done")
        val PinnedApps = stringPreferencesKey("pinned_apps")
    }
}

/** 순서 있는 문자열 목록 구분자 — ComponentName에는 줄바꿈이 없다 */
private const val ListSeparator = "\n"

/** 알 수 없는 값(이전 버전 등)은 기본값으로 */
private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
