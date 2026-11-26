package com.desklauncher.feature.memo.data

import com.desklauncher.core.database.dao.MemoDao
import com.desklauncher.core.database.dao.ProjectDao
import com.desklauncher.core.database.entity.MemoEntity
import com.desklauncher.core.database.entity.ProjectEntity
import com.desklauncher.feature.memo.domain.model.Memo
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
    private val memoDao: MemoDao,
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

    override val memoCounts: Flow<Map<String, Int>> =
        memoDao.observeCounts().map { list -> list.associate { it.projectId to it.count } }

    override fun project(id: String): Flow<Project?> = projectDao.observe(id).map { it?.toDomain() }

    override fun memos(projectId: String): Flow<List<Memo>> =
        memoDao.observeByProject(projectId).map { list -> list.map { it.toDomain() } }

    override suspend fun getMemo(id: String): Memo? = memoDao.get(id)?.toDomain()

    override suspend fun saveMemo(memo: Memo) {
        memoDao.upsert(memo.copy(updatedAt = clock.instant()).toEntity())
    }

    override suspend fun deleteMemo(id: String) = memoDao.delete(id)
}

internal fun MemoEntity.toDomain() = Memo(
    id = id, projectId = projectId, title = title, body = body, pinned = pinned,
    createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.ofEpochMilli(updatedAt),
)

internal fun Memo.toEntity() = MemoEntity(
    id = id, projectId = projectId, title = title, body = body, pinned = pinned,
    createdAt = createdAt.toEpochMilli(), updatedAt = updatedAt.toEpochMilli(),
)

internal fun ProjectEntity.toDomain() = Project(
    id = id, name = name, colorName = color, archived = archived, sortOrder = sortOrder,
    createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.ofEpochMilli(updatedAt),
)

internal fun Project.toEntity() = ProjectEntity(
    id = id, name = name, color = colorName, archived = archived, sortOrder = sortOrder,
    createdAt = createdAt.toEpochMilli(), updatedAt = updatedAt.toEpochMilli(),
)
