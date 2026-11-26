package com.desklauncher.feature.doodle.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskDoodlePaper
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.doodle.domain.Stroke

/**
 * 손가락 그리기 캔버스 (DOODLE-01).
 * - 그리는 중인 획만 매 이동마다 다시 그리고, 끝난 획은 [onStrokeFinished]로 넘긴다.
 * - 지우개는 오프스크린 레이어에 BlendMode.Clear로 그려 아래 획만 지운다(종이 배경은 레이어 밖).
 * - 이동 이벤트 사이의 historical 점까지 모아 빠른 선도 끊기지 않게 한다.
 */
@Composable
internal fun DoodleCanvas(
    strokes: List<Stroke>,
    newStroke: () -> Stroke,
    onStrokeFinished: (Stroke) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onSizeChanged: (width: Int, height: Int) -> Unit = { _, _ -> },
) {
    // 그리는 중인 획: 점은 버퍼에 쌓고 version만 올려 '그리기'만 다시 일어나게 한다(재구성 없음)
    val buffer = remember { ArrayList<Float>(512) }
    var template by remember { mutableStateOf<Stroke?>(null) }
    var version by remember { mutableIntStateOf(0) }
    val currentNewStroke by rememberUpdatedState(newStroke)
    val currentOnFinished by rememberUpdatedState(onStrokeFinished)
    // 끝난 획의 Path는 목록이 바뀔 때만 만든다
    val paths = remember(strokes) { strokes.map { it to it.toPathOrNull() } }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(DeskDoodlePaper)
            .onSizeChanged { onSizeChanged(it.width, it.height) }
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    buffer.clear()
                    buffer += down.position.x
                    buffer += down.position.y
                    template = currentNewStroke()
                    version++
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        change.historical.forEach {
                            buffer += it.position.x
                            buffer += it.position.y
                        }
                        buffer += change.position.x
                        buffer += change.position.y
                        version++
                        change.consume()
                        if (!change.pressed) break
                    }
                    val finished = template?.copy(points = buffer.toList())
                    template = null
                    buffer.clear()
                    version++
                    finished?.let(currentOnFinished)
                }
            },
    ) {
        paths.forEach { (stroke, path) -> drawStroke(stroke, path) }
        @Suppress("UNUSED_EXPRESSION") version // 읽어서 그리기 무효화 구독
        template?.let { drawStroke(it.copy(points = buffer.toList())) }
    }
}

internal fun DrawScope.drawStroke(stroke: Stroke, path: Path? = stroke.toPathOrNull()) {
    val color = Color(stroke.argb)
    val blend = if (stroke.eraser) BlendMode.Clear else BlendMode.SrcOver
    if (path == null) {
        if (stroke.pointCount == 1) {
            drawCircle(color, radius = stroke.widthPx / 2f, center = Offset(stroke.points[0], stroke.points[1]), blendMode = blend)
        }
        return
    }
    drawPath(
        path = path,
        color = color,
        style = DrawStroke(width = stroke.widthPx, cap = StrokeCap.Round, join = StrokeJoin.Round),
        blendMode = blend,
    )
}

/** 점 사이 중점을 잇는 2차 베지어로 부드럽게. 점이 2개 미만이면 null */
internal fun Stroke.toPathOrNull(): Path? = if (pointCount < 2) null else Path().apply {
    val p = points
    moveTo(p[0], p[1])
    var i = 2
    while (i + 3 < p.size) {
        val midX = (p[i] + p[i + 2]) / 2f
        val midY = (p[i + 1] + p[i + 3]) / 2f
        quadraticTo(p[i], p[i + 1], midX, midY)
        i += 2
    }
    lineTo(p[p.size - 2], p[p.size - 1])
}

@DeskPreviews
@Composable
private fun DoodleCanvasPreview() {
    val sample = listOf(
        Stroke(0xFF1F1B16.toInt(), 8f, points = listOf(80f, 300f, 160f, 220f, 260f, 320f, 360f, 230f, 460f, 330f)),
        Stroke(0xFFC93C3C.toInt(), 16f, points = listOf(120f, 500f, 400f, 520f, 700f, 480f)),
        Stroke(0, 40f, eraser = true, points = listOf(260f, 200f, 260f, 400f)),
    )
    DeskTheme { DoodleCanvas(strokes = sample, newStroke = { Stroke(0, 4f) }, onStrokeFinished = {}, contentDescription = "그림판") }
}
