package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM project WHERE archived = 0 ORDER BY sortOrder ASC, createdAt ASC")
    fun observeActive(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM project ORDER BY archived ASC, sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM project WHERE id = :id")
    fun observe(id: String): Flow<ProjectEntity?>

    @Upsert
    suspend fun upsert(project: ProjectEntity)

    @Query("DELETE FROM project WHERE id = :id")
    suspend fun delete(id: String)
}
