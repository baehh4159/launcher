package com.desklauncher.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * 미색 종이 테마. surfaceContainer 단계도 종이 톤으로 맞춰 높이(tonal elevation)에 따른 색 번짐이 없게 하고,
 * outline/outlineVariant를 얇은 선 색으로 두어 HorizontalDivider 등이 자동으로 선 토큰을 쓰게 한다.
 */
private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = DustyBlueInk,
    onPrimary = PaperSurface,
    primaryContainer = DustyBlueContainer,
    onPrimaryContainer = DustyBlueOnContainer,
    secondary = SageInk,
    onSecondary = PaperSurface,
    secondaryContainer = SageContainer,
    onSecondaryContainer = Ink,
    tertiary = TerracottaInk,
    background = PaperBackground,
    onBackground = Ink,
    surface = PaperSurface,
    onSurface = Ink,
    surfaceVariant = PaperContainer,
    onSurfaceVariant = InkSecondary,
    surfaceTint = PaperSurface,
    surfaceBright = PaperSurface,
    surfaceDim = PaperContainerHigh,
    surfaceContainerLowest = PaperSurface,
    surfaceContainerLow = PaperContainerLow,
    surfaceContainer = PaperContainer,
    surfaceContainerHigh = PaperContainerHigh,
    surfaceContainerHighest = PaperContainerHighest,
    inverseSurface = Ink,
    inverseOnSurface = PaperBackground,
    outline = PaperLineStrong,
    outlineVariant = PaperLine,
    error = TerracottaInk,
    errorContainer = TerracottaContainer,
    onErrorContainer = Ink,
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = DustyBlueLight,
    onPrimary = NightBackground,
    primaryContainer = DustyBlueNightContainer,
    onPrimaryContainer = NightInk,
    secondary = SageLight,
    secondaryContainer = NightContainerHigh,
    onSecondaryContainer = NightInk,
    tertiary = TerracottaLight,
    background = NightBackground,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = NightContainer,
    onSurfaceVariant = NightInkSecondary,
    surfaceTint = NightSurface,
    surfaceBright = NightContainerHigh,
    surfaceDim = NightBackground,
    surfaceContainerLowest = NightBackground,
    surfaceContainerLow = NightContainerLow,
    surfaceContainer = NightContainer,
    surfaceContainerHigh = NightContainerHigh,
    surfaceContainerHighest = NightContainerHighest,
    inverseSurface = NightInk,
    inverseOnSurface = NightBackground,
    outline = NightLineStrong,
    outlineVariant = NightLine,
    error = TerracottaLight,
)

/** 책상 런처 테마. 책상 콘셉트 색을 유지하기 위해 동적 색상(Material You)은 쓰지 않는다. */
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
