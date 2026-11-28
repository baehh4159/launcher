package com.desklauncher.core.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.motion.LocalReducedMotion
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import kotlinx.coroutines.launch

/**
 * 책상 위 창 (PRD §9, DESIGN_LANGUAGE §5 '창'). [window]가 null이 아니면 막 위에 종이 창을 펼친다.
 * - 열기: 창을 연 칸 위치([origin])에서 펼쳐진다(beUI Bloom menu 스프링), 닫기: EaseDrawer 280ms
 * - 닫기 경로: 막 누르기, 시스템 뒤로가기, 창의 빈 곳을 오른쪽으로 밀기(120dp 또는 600dp/s)
 * - 시스템 바·키보드 영역은 창 바깥에서 소비하므로 안쪽 내용은 같은 여백을 다시 넣지 않는다
 * - 움직임 줄이기: 이동·크기 없이 150ms 페이드
 */
@Composable
fun <T : Any> DeskWindowHost(
    window: T?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    origin: TransformOrigin = TransformOrigin(0f, 0.7f),
    content: @Composable BoxScope.(T) -> Unit,
) {
    val reduced = LocalReducedMotion.current
    BackHandler(enabled = window != null, onBack = onDismiss)
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = window != null,
            enter = fadeIn(if (reduced) tween(DeskMotion.ReducedFadeMillis) else tween(DeskMotion.RevealMillis)),
            exit = fadeOut(if (reduced) tween(DeskMotion.ReducedFadeMillis) else DeskMotion.windowClose()),
        ) {
            WindowScrim(onDismiss = onDismiss)
        }
        // 닫히는 동안에도 내용이 남아 있어야 하므로 AnimatedContent로 이전 창을 유지한다
        AnimatedContent(
            targetState = window,
            transitionSpec = {
                val enter = if (reduced) fadeIn(tween(DeskMotion.ReducedFadeMillis)) else openTransition(origin)
                val exit = if (reduced) fadeOut(tween(DeskMotion.ReducedFadeMillis)) else closeTransition(origin)
                when {
                    initialState == null -> enter togetherWith ExitTransition.None
                    targetState == null -> EnterTransition.None togetherWith exit
                    else -> enter togetherWith exit
                }
            },
            contentKey = { it?.let { value -> value::class to value.hashCode() } },
            label = "deskWindow",
        ) { current ->
            if (current != null) {
                WindowFrame(onSwipeDismiss = onDismiss) { content(current) }
            }
        }
    }
}

private fun openTransition(origin: TransformOrigin): EnterTransition =
    scaleIn(DeskMotion.folder(), initialScale = 0.88f, transformOrigin = origin) +
        slideInHorizontally(DeskMotion.folder()) { fullWidth -> -fullWidth / 6 } +
        fadeIn(tween(DeskMotion.RevealMillis, easing = DeskMotion.EaseOut))

private fun closeTransition(origin: TransformOrigin): ExitTransition =
    scaleOut(DeskMotion.windowClose(), targetScale = 0.9f, transformOrigin = origin) +
        slideOutHorizontally(DeskMotion.windowClose()) { fullWidth -> -fullWidth / 6 } +
        fadeOut(DeskMotion.windowClose())

/** 창 뒤 막 — 누르면 닫는다. 낭독 대상 아님(닫기는 뒤로가기로) */
@Composable
private fun WindowScrim(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeskTheme.colors.scrim)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .clearAndSetSemantics { },
    )
}

/**
 * 종이 창 틀. 안쪽 요소가 먼저 쓴 터치는 건드리지 않고, 빈 곳에서 오른쪽으로 끈 것만 받는다 —
 * 본문 선택·스크롤·버튼과 겹치지 않게 (PRD §9).
 */
@Composable
private fun WindowFrame(onSwipeDismiss: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    val swipe = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val distancePx = with(density) { SwipeDismissDistance.toPx() }
    val velocityPx = with(density) { SwipeDismissVelocity.toPx() }
    val currentOnDismiss by rememberUpdatedState(onSwipeDismiss)
    val colors = DeskTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(WindowMargin),
    ) {
        Surface(
            shape = DeskShape.window,
            color = colors.paper,
            border = BorderStroke(DeskLine.thin, colors.lineStrong),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = swipe.value
                    // 끄는 동안 서류철 쪽(왼쪽)을 기준으로 아주 조금 작아진다 — 다시 꽂힐 것을 미리 보여 줌
                    val shrink = 1f - 0.06f * (swipe.value / size.width).coerceIn(0f, 1f)
                    scaleX = shrink
                    scaleY = shrink
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val start = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                            if (over > 0f) change.consume()
                        } ?: return@awaitEachGesture
                        val tracker = VelocityTracker()
                        tracker.addPointerInputChange(start)
                        // snapTo는 비동기로 반영되므로 누적값은 여기서 들고 간다
                        var offset = (swipe.value + start.positionChange().x).coerceAtLeast(0f)
                        scope.launch { swipe.snapTo(offset) }
                        val completed = horizontalDrag(start.id) { change ->
                            tracker.addPointerInputChange(change)
                            offset = (offset + change.positionChange().x).coerceAtLeast(0f)
                            val next = offset
                            scope.launch { swipe.snapTo(next) }
                            change.consume()
                        }
                        val velocity = if (completed) tracker.calculateVelocity().x else 0f
                        if (completed && WindowSwipe.shouldDismiss(offset, velocity, distancePx, velocityPx)) {
                            currentOnDismiss()
                        } else {
                            scope.launch { swipe.animateTo(0f, DeskMotion.panel()) }
                        }
                    }
                },
        ) {
            Box(content = content)
        }
    }
}

/** 창 둘레로 책상이 보이는 여백 [실험] (PRD §9) */
private val WindowMargin = 10.dp
private val SwipeDismissDistance = 120.dp
private val SwipeDismissVelocity = 600.dp

@DeskPreviews
@Composable
private fun DeskWindowHostPreview() {
    DeskTheme {
        Box(Modifier.fillMaxSize().background(DeskTheme.colors.desk)) {
            DeskWindowHost(window = "preview", onDismiss = {}) {
                Text("창 내용", style = DeskType.body, color = DeskTheme.colors.onPaper, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
