package com.desklauncher.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.desklauncher.core.designsystem.motion.LocalReducedMotion
import com.desklauncher.core.designsystem.motion.rememberReducedMotion

/**
 * 앱 전체 테마. 의미 색은 [DeskTheme.colors], 기본 Material 컴포넌트는 같은 색에서 만든 colorScheme을 쓴다.
 * 움직임 줄이기 상태도 여기서 한 번 읽어 내려보낸다 (DESIGN_LANGUAGE D3).
 */
@Composable
fun DeskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkDeskColors else LightDeskColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.ink, onPrimary = colors.onInk,
            background = colors.desk, onBackground = colors.onPaper,
            surface = colors.paper, onSurface = colors.onPaper,
            surfaceVariant = colors.desk, onSurfaceVariant = colors.muted,
            surfaceContainer = colors.paper, surfaceContainerHigh = colors.paper,
            surfaceContainerHighest = colors.paper, surfaceContainerLow = colors.paper,
            outline = colors.lineStrong, outlineVariant = colors.line, scrim = colors.scrim,
        )
    } else {
        lightColorScheme(
            primary = colors.ink, onPrimary = colors.onInk,
            background = colors.desk, onBackground = colors.onPaper,
            surface = colors.paper, onSurface = colors.onPaper,
            surfaceVariant = colors.desk, onSurfaceVariant = colors.muted,
            surfaceContainer = colors.paper, surfaceContainerHigh = colors.paper,
            surfaceContainerHighest = colors.paper, surfaceContainerLow = colors.paper,
            outline = colors.lineStrong, outlineVariant = colors.line, scrim = colors.scrim,
        )
    }
    CompositionLocalProvider(
        LocalDeskColors provides colors,
        LocalReducedMotion provides rememberReducedMotion(),
    ) {
        MaterialTheme(colorScheme = scheme, typography = DeskMaterialTypography, content = content)
    }
}

object DeskTheme {
    val colors: DeskColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDeskColors.current
}
