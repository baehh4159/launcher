package com.desklauncher.feature.scanner.ui.component

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.storage.DeskStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** 썸네일 파일(상대 경로) → 이미지. 화면 전용이라 Route에서 EntryPoint로 꺼내 쓴다. */
fun interface ThumbnailLoader {
    suspend fun load(relativePath: String): ImageBitmap?
}

internal class FileThumbnailLoader @Inject constructor(
    private val storage: DeskStorage,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ThumbnailLoader {
    override suspend fun load(relativePath: String): ImageBitmap? = withContext(ioDispatcher) {
        storage.resolve(relativePath)?.takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() }
    }
}
