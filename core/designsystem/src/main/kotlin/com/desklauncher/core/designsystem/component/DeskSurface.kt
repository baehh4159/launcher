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
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme

/** 책상 바탕: 무늬 없는 미색 종이 한 장. */
@Composable
fun DeskSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeskTheme.colors.desk),
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
                color = DeskTheme.colors.onDesk,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
