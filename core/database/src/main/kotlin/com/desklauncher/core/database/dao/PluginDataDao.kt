package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.PluginDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PluginDataDao {

    @Query("SELECT * FROM plugin_data WHERE pluginId = :pluginId")
    fun observeAll(pluginId: String): Flow<List<PluginDataEntity>>

    @Query("SELECT value FROM plugin_data WHERE pluginId = :pluginId AND `key` = :key")
    fun observe(pluginId: String, key: String): Flow<String?>

    @Query("SELECT value FROM plugin_data WHERE pluginId = :pluginId AND `key` = :key")
    suspend fun get(pluginId: String, key: String): String?

    @Upsert
    suspend fun upsert(entity: PluginDataEntity)

    @Query("DELETE FROM plugin_data WHERE pluginId = :pluginId AND `key` = :key")
    suspend fun delete(pluginId: String, key: String)

    @Query("DELETE FROM plugin_data WHERE pluginId = :pluginId")
    suspend fun deleteAll(pluginId: String)
}
