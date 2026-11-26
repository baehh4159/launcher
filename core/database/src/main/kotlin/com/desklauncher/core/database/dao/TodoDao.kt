package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todo ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<TodoEntity>>

    /** 캘린더 등 다른 도구가 읽기 전용으로 쓰는 조회 (PRD §7.2). */
    @Query("SELECT * FROM todo WHERE dueDate IS NOT NULL AND dueDate BETWEEN :fromEpochDay AND :toEpochDay")
    fun observeDueBetween(fromEpochDay: Long, toEpochDay: Long): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo WHERE projectId = :projectId AND isDone = 0")
    fun observeOpenByProject(projectId: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo WHERE id = :id")
    suspend fun get(id: String): TodoEntity?

    @Upsert
    suspend fun upsert(todo: TodoEntity)

    @Query("DELETE FROM todo WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE todo SET isDone = :done, completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDone(id: String, done: Boolean, completedAt: Long?, updatedAt: Long)
}
