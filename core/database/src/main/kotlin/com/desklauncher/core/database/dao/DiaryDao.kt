package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entry ORDER BY date DESC")
    fun observeAll(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entry WHERE date = :epochDay")
    suspend fun getByDate(epochDay: Long): DiaryEntryEntity?

    @Upsert
    suspend fun upsert(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entry WHERE date = :epochDay")
    suspend fun deleteByDate(epochDay: Long)
}
