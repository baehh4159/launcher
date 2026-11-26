package com.desklauncher.core.designsystem.scrub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 앱 서랍 초성/알파벳 인덱스 스트립 (SHELL-07). [IndexTabStrip]과 같은 상태·제스처·햅틱을 쓴다.
 * 포커스가 바뀔 때 목록을 옮기는 일은 호출부가 [IndexScrubState.focusedIndex]를 보고 처리한다.
 */
@Composable
fun IndexLetterStrip(
    letters: List<String>,
    state: IndexScrubState,
    onOpen: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = letters.size
    SideEffect { state.itemCount = count }
    if (count == 0) return
    IndexScrubHaptics(state)

    val active = animateFloatAsState(
        targetValue = if (state.phase == ScrubPhase.Idle) 0f else 1f,
        animationSpec = tween(DeskMotion.PhaseFadeMillis),
        label = "letterStripActive",
    )

    BoxWithConstraints(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
        val extent = (maxHeight / count).coerceIn(LetterMinExtent, LetterMaxExtent)
        val density = LocalDensity.current
        val extentPx = with(density) { extent.toPx() }
        val popPx = with(density) { LetterPop.toPx() }
        val cancelPx = with(density) { DeskMotion.CancelDistance.toPx() }
        val exclusionPx = with(density) { DeskMotion.GestureExclusionMaxHeight.toPx() }
        val color = MaterialTheme.colorScheme.primary
        val onColor = MaterialTheme.colorScheme.onPrimary

        Box(modifier = Modifier.width(LetterTouchWidth).height(extent * count)) {
            letters.forEachIndexed { index, letter ->
                val pop = { active.value * IndexScrubMath.popFraction(index, state.focus, sigma = 0.9f) }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = -(popPx * pop()).roundToInt(),
                                y = (index * extentPx).roundToInt(),
                            )
                        }
                        .size(width = LetterTouchWidth, height = extent)
                        .semantics {
                            role = Role.Button
                            contentDescription = letter
                            onClick {
                                onOpen(index)
                                true
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                val p = pop()
                                alpha = p
                                scaleX = 0.6f + 0.6f * p
                                scaleY = 0.6f + 0.6f * p
                            }
                            .background(color, CircleShape),
                    )
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.graphicsLayer { alpha = 1f - pop() },
                    )
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelLarge,
                        color = onColor,
                        modifier = Modifier.graphicsLayer {
                            val p = pop()
                            alpha = p
                            scaleX = 0.8f + 0.5f * p
                            scaleY = 0.8f + 0.5f * p
                        },
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LetterTouchWidth)
                    .align(Alignment.TopEnd)
                    .systemGestureExclusion { coordinates ->
                        val height = coordinates.size.height.toFloat()
                        val excluded = min(height, exclusionPx)
                        val top = (height - excluded) / 2f
                        Rect(0f, top, coordinates.size.width.toFloat(), top + excluded)
                    }
                    .indexScrubGesture(state, extentPx, cancelPx, onOpen),
            )
        }
    }
}

private val LetterMinExtent = 14.dp
private val LetterMaxExtent = 26.dp
private val LetterTouchWidth = 40.dp
private val LetterPop = 36.dp

@DeskPreviews
@Composable
private fun IndexLetterStripPreview() {
    DeskTheme {
        Box(Modifier.fillMaxHeight()) {
            IndexLetterStrip(
                letters = listOf("ㄱ", "ㄴ", "ㄷ", "ㅁ", "ㅅ", "ㅇ", "ㅈ", "ㅋ", "ㅎ", "A", "G", "N", "Y", "#"),
                state = rememberIndexScrubState(initialFocus = 5.2f, initialPhase = ScrubPhase.Scrubbing),
                onOpen = {},
            )
        }
    }
}
