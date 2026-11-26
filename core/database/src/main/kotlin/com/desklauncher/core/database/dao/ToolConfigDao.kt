package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.desklauncher.core.database.entity.ToolConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolConfigDao {

    @Query("SELECT * FROM tool_config ORDER BY sortOrder ASC, toolId ASC")
    fun observeAll(): Flow<List<ToolConfigEntity>>

    /** 이미 있는 도구는 건드리지 않는다 — 사용자 설정을 덮어쓰지 않기 위함. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(configs: List<ToolConfigEntity>)

    @Query("UPDATE tool_config SET enabled = :enabled WHERE toolId = :toolId")
    suspend fun setEnabled(toolId: String, enabled: Boolean)

    @Query("UPDATE tool_config SET sortOrder = :sortOrder WHERE toolId = :toolId")
    suspend fun setSortOrder(toolId: String, sortOrder: Int)

    @Query("UPDATE tool_config SET tabColor = :tabColor WHERE toolId = :toolId")
    suspend fun setTabColor(toolId: String, tabColor: String?)

    @Transaction
    suspend fun reorder(orderedToolIds: List<String>) {
        orderedToolIds.forEachIndexed { index, toolId -> setSortOrder(toolId, index) }
    }
}
