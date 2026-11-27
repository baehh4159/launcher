package com.desklauncher.feature.files.data

import android.content.Context
import android.content.Intent
import android.provider.OpenableColumns
import androidx.core.net.toUri
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.database.dao.DocumentDao
import com.desklauncher.core.database.dao.DoodleDao
import com.desklauncher.core.database.dao.FileBookmarkDao
import com.desklauncher.core.database.entity.FileBookmarkEntity
import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import com.desklauncher.feature.files.domain.repository.FilesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

/** 서류정리함·낙서장 데이터를 읽기 전용으로 모아 보여 준다 (PRD §7.2: feature 간 직접 의존 대신 DAO). */
internal class RoomFilesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val documentDao: DocumentDao,
    private val doodleDao: DoodleDao,
    private val bookmarkDao: FileBookmarkDao,
    private val clock: Clock,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
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
        FileCategory.Bookmarks -> bookmarkDao.observeAll().map { bookmarks ->
            bookmarks.map {
                FileItem(
                    id = it.id,
                    name = it.displayName,
                    category = category,
                    location = it.uri,
                    mimeType = it.mimeType ?: "*/*",
                    sizeBytes = it.sizeBytes,
                    modifiedAt = Instant.ofEpochMilli(it.addedAt),
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

    override suspend fun addBookmark(uri: String): Boolean = withContext(ioDispatcher) {
        val parsed = uri.toUri()
        val resolver = context.contentResolver
        runCatching {
            // 앱을 다시 켜도 읽을 수 있게 영구 권한을 받는다 (공급자가 허용하지 않으면 예외)
            resolver.takePersistableUriPermission(parsed, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            var name: String? = null
            var size: Long? = null
            resolver.query(parsed, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0)
                    size = if (cursor.isNull(1)) null else cursor.getLong(1)
                }
            }
            bookmarkDao.insert(
                FileBookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    uri = uri,
                    displayName = name ?: parsed.lastPathSegment ?: uri,
                    mimeType = resolver.getType(parsed),
                    sizeBytes = size,
                    addedAt = clock.millis(),
                ),
            )
        }.isSuccess
    }

    private companion object {
        val NameFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm")
    }
}
