package com.desklauncher.core.toolregistry

import com.desklauncher.core.database.dao.PluginDataDao
import com.desklauncher.core.database.entity.PluginDataEntity
import com.desklauncher.core.toolapi.PluginStorage
import com.desklauncher.core.toolapi.PluginStorageProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** `plugin_data` 표 위의 [PluginStorage]. 플러그인 id로 칸을 나눈다. */
internal class RoomPluginStorage(
    private val pluginId: String,
    private val dao: PluginDataDao,
    private val clock: () -> Long,
) : PluginStorage {

    override fun observe(key: String): Flow<String?> = dao.observe(pluginId, key).distinctUntilChanged()

    override fun observeAll(): Flow<Map<String, String>> =
        dao.observeAll(pluginId).map { rows -> rows.associate { it.key to it.value } }.distinctUntilChanged()

    override suspend fun get(key: String): String? = dao.get(pluginId, key)

    override suspend fun put(key: String, value: String?) {
        if (value == null) {
            dao.delete(pluginId, key)
        } else {
            dao.upsert(PluginDataEntity(pluginId = pluginId, key = key, value = value, updatedAt = clock()))
        }
    }

    override suspend fun clear() {
        dao.deleteAll(pluginId)
    }
}

@Singleton
internal class RoomPluginStorageProvider @Inject constructor(
    private val dao: PluginDataDao,
) : PluginStorageProvider {
    override fun forTool(toolId: String): PluginStorage =
        RoomPluginStorage(pluginId = toolId, dao = dao, clock = System::currentTimeMillis)
}
