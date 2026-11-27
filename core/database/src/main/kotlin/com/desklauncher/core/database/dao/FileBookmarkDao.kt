package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.desklauncher.core.database.entity.FileBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileBookmarkDao {
    @Query("SELECT * FROM file_bookmark ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FileBookmarkEntity>>

    /** 같은 URI를 다시 고르면 무시 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: FileBookmarkEntity)

    @Query("SELECT * FROM file_bookmark WHERE id = :id")
    suspend fun get(id: String): FileBookmarkEntity?

    @Query("DELETE FROM file_bookmark WHERE id = :id")
    suspend fun delete(id: String)
}
