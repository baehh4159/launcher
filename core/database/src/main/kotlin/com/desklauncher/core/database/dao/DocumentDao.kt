package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.DocumentEntity
import com.desklauncher.core.database.entity.DocumentFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM document ORDER BY createdAt DESC")
    fun observeDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM document WHERE id = :id")
    suspend fun getDocument(id: String): DocumentEntity?

    @Query("SELECT * FROM document WHERE id = :id")
    fun observeDocument(id: String): Flow<DocumentEntity?>

    @Upsert
    suspend fun upsertDocument(document: DocumentEntity)

    @Query("UPDATE document SET title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun rename(id: String, title: String, updatedAt: Long)

    @Query("UPDATE document SET folderId = :folderId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun move(id: String, folderId: String?, updatedAt: Long)

    @Query("DELETE FROM document WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("SELECT * FROM document_folder ORDER BY sortOrder ASC, createdAt ASC")
    fun observeFolders(): Flow<List<DocumentFolderEntity>>

    @Upsert
    suspend fun upsertFolder(folder: DocumentFolderEntity)

    @Query("DELETE FROM document_folder WHERE id = :id")
    suspend fun deleteFolder(id: String)
}
