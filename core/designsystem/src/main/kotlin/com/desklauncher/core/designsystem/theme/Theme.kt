package com.desklauncher.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Walnut40,
    onPrimary = Paper98,
    primaryContainer = Walnut90,
    onPrimaryContainer = Walnut10,
    secondary = Sage40,
    secondaryContainer = Sage80,
    background = Paper95,
    onBackground = Ink10,
    surface = Paper98,
    onSurface = Ink10,
    onSurfaceVariant = Ink30,
    error = Error40,
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Walnut80,
    onPrimary = Walnut10,
    primaryContainer = Walnut30,
    onPrimaryContainer = Walnut90,
    secondary = Sage80,
    background = Night10,
    onBackground = Ink90,
    surface = Night15,
    onSurface = Ink90,
    onSurfaceVariant = Ink80,
    error = Error80,
)

/**
 * 책상 런처 테마. 책상 콘셉트 색을 유지하기 위해 동적 색상(Material You)은 쓰지 않는다.
 */
@Composable
fun DeskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDeskColors provides if (darkTheme) DarkDeskColors else LightDeskColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            content = content,
        )
    }
}

/** `DeskTheme.colors`로 책상 전용 색에 접근한다. */
object DeskTheme {
    val colors: DeskColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDeskColors.current
}
