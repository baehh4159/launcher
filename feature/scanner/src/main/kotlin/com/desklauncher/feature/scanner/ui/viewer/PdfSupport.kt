package com.desklauncher.feature.scanner.ui.viewer

import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.storage.DeskStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.Closeable
import javax.inject.Inject

/** PDF 보기·공유 (DOC-04). 화면 전용이라 Route에서 EntryPoint로 꺼내 쓴다. */
class PdfSupport @Inject internal constructor(
    private val storage: DeskStorage,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun open(relativePath: String): PdfHandle? = withContext(ioDispatcher) {
        val file = storage.resolve(relativePath)?.takeIf { it.exists() } ?: return@withContext null
        runCatching {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfHandle(fd, PdfRenderer(fd), ioDispatcher)
        }.getOrNull()
    }

    /** 다른 앱으로 보내기. 받는 앱에는 이 파일 읽기 권한만 임시로 준다. */
    fun shareIntent(relativePath: String, title: String): Intent? {
        val file = storage.resolve(relativePath)?.takeIf { it.exists() } ?: return null
        val send = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, storage.shareUri(file))
            .putExtra(Intent.EXTRA_TITLE, title)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return Intent.createChooser(send, title)
    }
}

/** PdfRenderer는 한 번에 한 페이지만 열 수 있어 Mutex로 줄 세운다. */
class PdfHandle internal constructor(
    private val fd: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
    private val ioDispatcher: CoroutineDispatcher,
) : Closeable {
    private val mutex = Mutex()
    @Volatile
    private var closed = false

    val pageCount: Int = renderer.pageCount

    suspend fun render(index: Int, widthPx: Int): ImageBitmap? = withContext(ioDispatcher) {
        mutex.withLock {
            try {
                if (closed || index !in 0 until pageCount || widthPx <= 0) return@withLock null
                renderer.openPage(index).use { page ->
                    val height = (widthPx * page.height.toFloat() / page.width).toInt().coerceAtLeast(1)
                    val bitmap = createBitmap(widthPx, height)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap.asImageBitmap()
                }
            } finally {
                if (closed) release()
            }
        }
    }

    /**
     * 화면을 떠날 때(메인 스레드) 호출. 그리는 중이면 기다리지 않고 표시만 해 두고,
     * 그리기가 끝나는 쪽([render]의 finally)에서 닫는다.
     */
    override fun close() {
        closed = true
        if (mutex.tryLock()) {
            try {
                release()
            } finally {
                mutex.unlock()
            }
        }
    }

    private fun release() {
        runCatching { renderer.close() }
        runCatching { fd.close() }
    }
}
