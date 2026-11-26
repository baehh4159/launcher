package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.MemoEntity
import com.desklauncher.core.database.entity.ProjectMemoCount
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {

    @Query("SELECT * FROM memo WHERE projectId = :projectId ORDER BY pinned DESC, updatedAt DESC")
    fun observeByProject(projectId: String): Flow<List<MemoEntity>>

    @Query("SELECT projectId, COUNT(*) AS count FROM memo GROUP BY projectId")
    fun observeCounts(): Flow<List<ProjectMemoCount>>

    @Query("SELECT * FROM memo WHERE id = :id")
    suspend fun get(id: String): MemoEntity?

    @Upsert
    suspend fun upsert(memo: MemoEntity)

    @Query("DELETE FROM memo WHERE id = :id")
    suspend fun delete(id: String)
}
