package com.desklauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.HapticPreference
import com.desklauncher.core.datastore.SettingsRepository
import com.desklauncher.core.datastore.ThemePreference
import com.desklauncher.core.designsystem.haptics.HapticLevel
import com.desklauncher.core.designsystem.haptics.LocalDeskHapticLevel
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.navigation.DeskNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

/** 단일 액티비티 + 기본 런처(HOME) 진입점. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var toolRegistry: ToolRegistry

    @Inject
    lateinit var settingsRepository: SettingsRepository

    // singleTask라 홈 버튼은 onNewIntent로 들어온다 (SHELL-06).
    private val homePressEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val homePresses = homePressEvents.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // null = 아직 읽는 중. 이때 온보딩 여부를 판단하지 않는다(매 실행 깜빡임 방지).
            val loaded by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = null)
            val settings = loaded ?: DeskSettings()
            val darkTheme = when (settings.theme) {
                ThemePreference.System -> isSystemInDarkTheme()
                ThemePreference.Light -> false
                ThemePreference.Dark -> true
            }
            // 앱 테마를 시스템과 다르게 고르면 상태바 아이콘 색도 맞춘다.
            DisposableEffect(darkTheme) {
                val style = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            CompositionLocalProvider(LocalDeskHapticLevel provides settings.haptic.toHapticLevel()) {
                DeskTheme(darkTheme = darkTheme) {
                    DeskNavHost(
                        toolRegistry = toolRegistry,
                        homePresses = homePresses,
                        onboardingCompleted = loaded?.onboardingCompleted,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.isHomeIntent()) homePressEvents.tryEmit(Unit)
    }

    private fun Intent.isHomeIntent(): Boolean =
        action == Intent.ACTION_MAIN && hasCategory(Intent.CATEGORY_HOME)
}

private fun HapticPreference.toHapticLevel(): HapticLevel = when (this) {
    HapticPreference.Off -> HapticLevel.Off
    HapticPreference.Light -> HapticLevel.Light
    HapticPreference.Default -> HapticLevel.Default
}
