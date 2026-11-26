package com.desklauncher.feature.doodle.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.database.dao.DoodleDao
import com.desklauncher.core.database.entity.DoodleEntity
import com.desklauncher.core.designsystem.theme.DeskDoodlePaper
import com.desklauncher.core.storage.DeskStorage
import com.desklauncher.feature.doodle.domain.Stroke
import com.desklauncher.feature.doodle.domain.model.Doodle
import com.desklauncher.feature.doodle.domain.model.DoodleFile
import com.desklauncher.feature.doodle.domain.repository.DoodleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

internal class FileDoodleRepository @Inject constructor(
    private val dao: DoodleDao,
    private val storage: DeskStorage,
    private val clock: Clock,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : DoodleRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override val doodles: Flow<List<Doodle>> = dao.observeAll().map { list ->
        list.map {
            Doodle(it.id, it.title, it.thumbnailPath, it.canvasWidth, it.canvasHeight, Instant.ofEpochMilli(it.createdAt), Instant.ofEpochMilli(it.updatedAt))
        }
    }

    override suspend fun load(id: String): DoodleFile? = withContext(ioDispatcher) {
        val entity = dao.get(id) ?: return@withContext null
        val file = storage.resolve(entity.strokesPath)?.takeIf { it.exists() } ?: return@withContext null
        runCatching { json.decodeFromString<DoodleFile>(file.readText()) }.getOrNull()
    }

    override suspend fun save(id: String?, file: DoodleFile): String = withContext(ioDispatcher) {
        val doodleId = id ?: UUID.randomUUID().toString()
        val strokesFile = storage.doodleStrokes(doodleId)
        val imageFile = storage.doodleImage(doodleId)
        // 임시 파일에 쓰고 바꿔치기 — 저장 중 꺼져도 이전 그림이 깨지지 않게
        val tmp = File(strokesFile.path + ".tmp")
        tmp.writeText(json.encodeToString(DoodleFile.serializer(), file))
        tmp.renameTo(strokesFile)
        val bitmap = render(file)
        imageFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()

        val now = clock.millis()
        val existing = dao.get(doodleId)
        dao.upsert(
            DoodleEntity(
                id = doodleId,
                title = existing?.title.orEmpty(),
                strokesPath = storage.relativePath(strokesFile),
                thumbnailPath = storage.relativePath(imageFile),
                canvasWidth = file.width,
                canvasHeight = file.height,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            ),
        )
        doodleId
    }

    override suspend fun delete(id: String) = withContext(ioDispatcher) {
        val existing = dao.get(id) ?: return@withContext
        dao.delete(id)
        storage.delete(existing.strokesPath)
        storage.delete(existing.thumbnailPath)
    }

    /** 화면과 같은 방식으로 그린다: 투명 레이어에 획(지우개 = CLEAR) → 종이색 위에 합성 */
    private fun render(file: DoodleFile): Bitmap {
        val width = file.width.coerceAtLeast(1)
        val height = file.height.coerceAtLeast(1)
        val layer = createBitmap(width, height)
        val canvas = Canvas(layer)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        file.strokes.forEach { stroke -> canvas.drawStroke(stroke, paint) }
        val result = createBitmap(width, height)
        Canvas(result).apply {
            drawColor(DeskDoodlePaper.toArgb())
            drawBitmap(layer, 0f, 0f, null)
        }
        layer.recycle()
        return result
    }

    private fun Canvas.drawStroke(stroke: Stroke, paint: Paint) {
        paint.color = stroke.argb
        paint.strokeWidth = stroke.widthPx
        paint.xfermode = if (stroke.eraser) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
        val p = stroke.points
        if (stroke.pointCount == 1) {
            paint.style = Paint.Style.FILL
            drawCircle(p[0], p[1], stroke.widthPx / 2f, paint)
            paint.style = Paint.Style.STROKE
            return
        }
        if (stroke.pointCount < 2) return
        val path = Path().apply {
            moveTo(p[0], p[1])
            var i = 2
            while (i + 3 < p.size) {
                quadTo(p[i], p[i + 1], (p[i] + p[i + 2]) / 2f, (p[i + 1] + p[i + 3]) / 2f)
                i += 2
            }
            lineTo(p[p.size - 2], p[p.size - 1])
        }
        drawPath(path, paint)
    }
}
