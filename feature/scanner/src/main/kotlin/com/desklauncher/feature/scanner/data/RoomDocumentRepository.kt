package com.desklauncher.feature.scanner.data

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Size
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.database.dao.DocumentDao
import com.desklauncher.core.database.entity.DocumentEntity
import com.desklauncher.core.database.entity.DocumentFolderEntity
import com.desklauncher.core.storage.DeskStorage
import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.domain.model.DocumentSource
import com.desklauncher.feature.scanner.domain.model.ScanResult
import com.desklauncher.feature.scanner.domain.repository.DocumentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

internal class RoomDocumentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DocumentDao,
    private val storage: DeskStorage,
    private val clock: Clock,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : DocumentRepository {

    private val resolver: ContentResolver get() = context.contentResolver

    override val documents: Flow<List<Document>> = dao.observeDocuments().map { list -> list.map { it.toDomain() } }

    override val folders: Flow<List<DocumentFolder>> = dao.observeFolders().map { list -> list.map { it.toDomain() } }

    override suspend fun importScan(result: ScanResult, folderId: String?): Document = withContext(ioDispatcher) {
        val id = UUID.randomUUID().toString()
        val pdf = storage.documentPdf(id)
        try {
            copy(result.pdfUri.toUri(), pdf)
            val thumbnail = storage.documentThumbnail(id)
            val thumbOk = runCatching {
                val image = result.firstPageImageUri?.let { decodeScaled(it.toUri()) } ?: renderFirstPage(pdf)
                thumbnail.outputStream().use { image.compress(Bitmap.CompressFormat.JPEG, ThumbQuality, it) }
                image.recycle()
            }.isSuccess
            val now = clock.instant()
            val document = Document(
                id = id,
                folderId = folderId,
                title = defaultTitle(now),
                pdfPath = storage.relativePath(pdf),
                thumbnailPath = if (thumbOk) storage.relativePath(thumbnail) else null,
                pageCount = result.pageCount,
                sizeBytes = pdf.length(),
                source = DocumentSource.Scan,
                createdAt = now,
                updatedAt = now,
            )
            dao.upsertDocument(document.toEntity())
            document
        } catch (error: Exception) {
            // 반쯤 만든 파일은 남기지 않는다
            pdf.delete()
            storage.documentThumbnail(id).delete()
            throw error
        }
    }

    private fun copy(source: Uri, target: File) {
        val input = resolver.openInputStream(source) ?: error("스캔 결과를 열 수 없음: $source")
        input.use { stream -> target.outputStream().use { stream.copyTo(it) } }
    }

    private fun decodeScaled(uri: Uri): Bitmap =
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
            val scale = ThumbWidth.toFloat() / info.size.width
            if (scale < 1f) decoder.setTargetSize(ThumbWidth, (info.size.height * scale).toInt().coerceAtLeast(1))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }

    /** 페이지 이미지가 없을 때(PDF만 받은 경우) 첫 장을 직접 그린다 */
    private fun renderFirstPage(pdf: File): Bitmap =
        ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
            PdfRenderer(fd).use { renderer ->
                renderer.openPage(0).use { page ->
                    val size = Size(ThumbWidth, (ThumbWidth * page.height.toFloat() / page.width).toInt().coerceAtLeast(1))
                    createBitmap(size.width, size.height).also { bitmap ->
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }

    private fun defaultTitle(now: Instant): String =
        "스캔 " + now.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    private companion object {
        const val ThumbWidth = 480
        const val ThumbQuality = 85
    }
}

internal fun DocumentEntity.toDomain() = Document(
    id = id, folderId = folderId, title = title, pdfPath = pdfPath, thumbnailPath = thumbnailPath,
    pageCount = pageCount, sizeBytes = sizeBytes,
    source = if (source == "IMPORT") DocumentSource.Import else DocumentSource.Scan,
    createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.ofEpochMilli(updatedAt),
)

internal fun Document.toEntity() = DocumentEntity(
    id = id, folderId = folderId, title = title, pdfPath = pdfPath, thumbnailPath = thumbnailPath,
    pageCount = pageCount, sizeBytes = sizeBytes, source = source.name.uppercase(),
    createdAt = createdAt.toEpochMilli(), updatedAt = updatedAt.toEpochMilli(),
)

internal fun DocumentFolderEntity.toDomain() = DocumentFolder(id = id, name = name, createdAt = Instant.ofEpochMilli(createdAt))
