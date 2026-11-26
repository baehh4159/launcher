package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.DoodleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoodleDao {
    @Query("SELECT * FROM doodle ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DoodleEntity>>

    @Query("SELECT * FROM doodle WHERE id = :id")
    suspend fun get(id: String): DoodleEntity?

    @Upsert
    suspend fun upsert(doodle: DoodleEntity)

    @Query("DELETE FROM doodle WHERE id = :id")
    suspend fun delete(id: String)
}
