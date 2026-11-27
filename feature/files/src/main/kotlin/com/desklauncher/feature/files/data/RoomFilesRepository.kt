package com.desklauncher.feature.files.data

import com.desklauncher.core.database.dao.DocumentDao
import com.desklauncher.core.database.dao.DoodleDao
import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import com.desklauncher.feature.files.domain.repository.FilesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** 서류정리함·낙서장 데이터를 읽기 전용으로 모아 보여 준다 (PRD §7.2: feature 간 직접 의존 대신 DAO). */
internal class RoomFilesRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val doodleDao: DoodleDao,
) : FilesRepository {

    override fun files(category: FileCategory): Flow<List<FileItem>> = when (category) {
        FileCategory.Scans -> documentDao.observeDocuments().map { docs ->
            docs.map {
                FileItem(
                    id = it.id,
                    name = "${it.title}.pdf",
                    category = category,
                    location = it.pdfPath,
                    mimeType = "application/pdf",
                    sizeBytes = it.sizeBytes,
                    modifiedAt = Instant.ofEpochMilli(it.updatedAt),
                )
            }
        }
        FileCategory.Doodles -> doodleDao.observeAll().map { doodles ->
            doodles.map {
                val modified = Instant.ofEpochMilli(it.updatedAt)
                FileItem(
                    id = it.id,
                    name = it.title.ifBlank { "낙서 " + NameFormat.format(modified.atZone(ZoneId.systemDefault())) } + ".png",
                    category = category,
                    location = it.thumbnailPath,
                    mimeType = "image/png",
                    sizeBytes = null,
                    modifiedAt = modified,
                )
            }
        }
    }

    private companion object {
        val NameFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm")
    }
}
