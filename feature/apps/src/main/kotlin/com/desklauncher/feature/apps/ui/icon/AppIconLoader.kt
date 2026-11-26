package com.desklauncher.feature.apps.ui.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** (UI 전용) 앱 아이콘을 필요할 때만(보이는 줄만) 불러와 메모리에 캐시한다. */
@Singleton
class AppIconLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val cache = LruCache<String, ImageBitmap>(MaxCachedIcons)

    suspend fun load(componentKey: String, sizePx: Int): ImageBitmap? {
        cache.get(componentKey)?.let { return it }
        return withContext(ioDispatcher) {
            val component = ComponentName.unflattenFromString(componentKey) ?: return@withContext null
            val info = runCatching {
                launcherApps.getActivityList(component.packageName, Process.myUserHandle())
                    .firstOrNull { it.componentName == component }
            }.getOrNull() ?: return@withContext null
            val drawable = info.getBadgedIcon(context.resources.displayMetrics.densityDpi)
            drawable.toBitmap(sizePx, sizePx).asImageBitmap().also { cache.put(componentKey, it) }
        }
    }

    fun invalidate() = cache.evictAll()

    private companion object {
        const val MaxCachedIcons = 256
    }
}
