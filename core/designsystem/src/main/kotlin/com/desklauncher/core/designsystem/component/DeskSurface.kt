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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import kotlin.math.sin
import kotlin.random.Random

/** 책상 상판 배경: 세로 그라데이션 + 은은한 나뭇결. */
@Composable
fun DeskSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = DeskTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.deskTop, colors.deskBottom)))
            .woodGrain(colors.deskGrain),
        content = content,
    )
}

/**
 * 나뭇결: 고정 시드로 만든 완만한 곡선 몇 줄. 크기가 바뀔 때만 다시 계산한다(drawWithCache).
 * 이미지 리소스 없이 그려 APK 크기와 디코딩 비용을 아낀다.
 */
private fun Modifier.woodGrain(color: Color): Modifier = drawWithCache {
    val random = Random(GrainSeed)
    val lineCount = (size.height / 18.dp.toPx()).toInt().coerceAtLeast(1)
    val segments = 24
    val lines = List(lineCount) { index ->
        val baseY = (index + random.nextFloat()) * size.height / lineCount
        val amplitude = random.nextFloat() * 6.dp.toPx()
        val phase = random.nextFloat() * 6.28f
        val alpha = 0.35f + random.nextFloat() * 0.65f
        val stroke = (0.6f + random.nextFloat() * 1.2f).dp.toPx()
        val points = List(segments + 1) { step ->
            val x = size.width * step / segments
            Offset(x, baseY + amplitude * sin(phase + x / size.width * 6.28f))
        }
        Triple(points, alpha, stroke)
    }
    onDrawBehind {
        lines.forEach { (points, alpha, stroke) ->
            for (i in 0 until points.lastIndex) {
                drawLine(color.copy(alpha = color.alpha * alpha), points[i], points[i + 1], strokeWidth = stroke)
            }
        }
    }
}

private const val GrainSeed = 2026

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
