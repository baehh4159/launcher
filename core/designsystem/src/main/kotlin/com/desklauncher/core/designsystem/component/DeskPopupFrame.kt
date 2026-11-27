package com.desklauncher.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import com.desklauncher.core.designsystem.motion.DeskMotion
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme

/**
 * 책상 위에 펼친 서류철 창 (SHELL-05). 도구·앱 서랍을 전체 화면 대신 이 창 안에 띄운다.
 * 시스템 바·키보드 영역은 창 바깥에서 소비하므로, 안쪽 Scaffold/TopAppBar는 같은 여백을 다시 넣지 않는다.
 *
 * [onSwipeDismiss]가 있으면 창의 빈 곳을 왼쪽→오른쪽으로 밀어 닫을 수 있다. 창은 손가락을 따라가며 살짝 작아지고,
 * 충분히 끌거나 튕기면 닫힌다(닫히는 전환이 이어서 서류철로 다시 꽂는다). 안쪽 요소(목록·버튼·입력칸·캔버스·
 * 가로로 밀리는 행 등)가 먼저 쓴 터치는 건드리지 않으므로 '빈 곳'에서만 동작한다.
 */
@Composable
fun DeskPopupFrame(
    modifier: Modifier = Modifier,
    onSwipeDismiss: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val swipe = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissVelocityPx = with(density) { DeskMotion.PopupSwipeDismissVelocity.toPx() }
    val currentOnDismiss by rememberUpdatedState(onSwipeDismiss)

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(DeskPopupDefaults.Margin),
    ) {
        Surface(
            shape = DeskPopupDefaults.Shape,
            color = DeskTheme.colors.paper,
            border = BorderStroke(DeskLine.Hairline, DeskTheme.colors.lineStrong),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = swipe.value
                    val shrink = 1f - DeskMotion.PopupSwipeMaxShrink * PopupSwipe.progress(swipe.value, size.width)
                    scaleX = shrink
                    scaleY = shrink
                    // 서류철이 꽂힌 왼쪽을 기준으로 작아진다
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .then(
                    if (onSwipeDismiss == null) {
                        Modifier
                    } else {
                        Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                // 오른쪽으로 슬롭을 넘을 때만 시작. 안쪽 요소가 먼저 쓴 터치면 null.
                                val start = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                                    if (over > 0f) change.consume()
                                } ?: return@awaitEachGesture
                                val tracker = VelocityTracker()
                                tracker.addPointerInputChange(start)
                                // snapTo는 비동기로 반영되므로 누적값은 여기서 직접 들고 간다
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
                                val dismiss = completed && PopupSwipe.shouldDismiss(
                                    offsetPx = offset,
                                    widthPx = size.width.toFloat(),
                                    velocityPxPerSec = velocity,
                                    dismissFraction = DeskMotion.PopupSwipeDismissFraction,
                                    dismissVelocityPxPerSec = dismissVelocityPx,
                                )
                                if (dismiss) {
                                    currentOnDismiss?.invoke()
                                } else {
                                    scope.launch { swipe.animateTo(0f, DeskMotion.SettleSpring) }
                                }
                            }
                        }
                    },
                ),
        ) {
            Box(content = content)
        }
    }
}

/** 팝업 뒤 책상을 어둡게 덮는 막. 누르면 [onDismiss]. 낭독 대상이 아니다(닫기는 뒤로 버튼으로). */
@Composable
fun DeskPopupScrim(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = DeskPopupDefaults.ScrimAlpha))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .clearAndSetSemantics { },
    )
}

object DeskPopupDefaults {
    /** 창 둘레로 책상이 보이는 여백 — 도구 화면 면적을 최대한 남기면서 '창'으로 보이는 최소 폭 */
    val Margin = 10.dp
    val Shape = RoundedCornerShape(20.dp)
    const val ScrimAlpha = 0.32f
}

@DeskPreviews
@Composable
private fun DeskPopupFramePreview() {
    DeskTheme {
        DeskSurface {
            DeskPopupScrim(onDismiss = {})
            DeskPopupFrame {
                ToolTopBar(title = "할일정리", tabColor = DeskTabColor.Orange, backContentDescription = "책상으로", onBack = {})
                Text(
                    text = "도구 화면",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}
