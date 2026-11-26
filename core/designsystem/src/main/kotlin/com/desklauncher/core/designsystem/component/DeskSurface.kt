package com.desklauncher.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme

/** 책상 상판 배경. 질감(나뭇결) 레이어는 SHELL-02에서 추가한다. */
@Composable
fun DeskSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = DeskTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.deskTop, colors.deskBottom))),
        content = content,
    )
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun DeskSurfacePreview() {
    DeskTheme {
        DeskSurface {
            Text(
                text = "책상 위",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
