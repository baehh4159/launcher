package com.desklauncher.core.toolapi

import kotlinx.coroutines.flow.Flow

/**
 * 플러그인 전용 키-값 저장소. 플러그인마다 칸이 나뉘어 있어 다른 도구의 값을 볼 수 없다.
 * 앱의 Room DB(plugin_data 표)에 저장되므로 플러그인은 자기 표·마이그레이션을 만들 필요가 없다.
 * 구조가 있는 값은 kotlinx.serialization JSON 문자열로 넣는다.
 */
interface PluginStorage {
    fun observe(key: String): Flow<String?>
    fun observeAll(): Flow<Map<String, String>>
    suspend fun get(key: String): String?

    /** null이면 지운다 */
    suspend fun put(key: String, value: String?)

    suspend fun clear()
}

/** 도구 id별 [PluginStorage]. 여러 화면 플러그인은 Hilt로 이것을 주입받아 쓴다. */
interface PluginStorageProvider {
    fun forTool(toolId: String): PluginStorage
}
