package com.desklauncher.feature.memo.data

import com.desklauncher.core.database.dao.ProjectDao
import com.desklauncher.core.database.entity.ProjectEntity
import com.desklauncher.feature.memo.domain.model.Project
import com.desklauncher.feature.memo.domain.repository.MemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

internal class RoomMemoRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val clock: Clock,
) : MemoRepository {

    override val projects: Flow<List<Project>> = projectDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addProject(name: String, colorName: String): String {
        val now = clock.millis()
        val id = UUID.randomUUID().toString()
        projectDao.upsert(ProjectEntity(id = id, name = name.trim(), color = colorName, sortOrder = Int.MAX_VALUE / 2, createdAt = now, updatedAt = now))
        return id
    }

    override suspend fun updateProject(project: Project) {
        projectDao.upsert(project.copy(name = project.name.trim(), updatedAt = clock.instant()).toEntity())
    }

    override suspend fun deleteProject(id: String) = projectDao.delete(id)
}

internal fun ProjectEntity.toDomain() = Project(
    id = id, name = name, colorName = color, archived = archived, sortOrder = sortOrder,
    createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.ofEpochMilli(updatedAt),
)

internal fun Project.toEntity() = ProjectEntity(
    id = id, name = name, color = colorName, archived = archived, sortOrder = sortOrder,
    createdAt = createdAt.toEpochMilli(), updatedAt = updatedAt.toEpochMilli(),
)
