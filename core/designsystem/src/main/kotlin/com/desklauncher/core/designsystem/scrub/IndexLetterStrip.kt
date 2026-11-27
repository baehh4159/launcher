package com.desklauncher.core.designsystem.scrub

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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.component.DeskSurface
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 앱 서랍 초성/알파벳 인덱스 스트립 (SHELL-07) — 서류철 스트립과 같은 파도 스크럽.
 * 글자들이 손가락 주변에서 넓은 곡선을 그리며 왼쪽으로 휘고, 휜 글자 왼쪽에 원형 인디케이터가 따라온다.
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
    val active = rememberStripActive(state)

    BoxWithConstraints(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
        val extent = (maxHeight / count).coerceIn(LetterMinExtent, LetterMaxExtent)
        val density = LocalDensity.current
        val extentPx = with(density) { extent.toPx() }
        val waveShiftPx = with(density) { DeskMotion.LetterWaveShift.toPx() }
        val cancelPx = with(density) { DeskMotion.CancelDistance.toPx() }
        val exclusionPx = with(density) { DeskMotion.GestureExclusionMaxHeight.toPx() }
        val letterWidthPx = with(density) { LetterTouchWidth.toPx() }
        val indicatorPx = with(density) { DeskMotion.LetterIndicatorSize.toPx() }
        val gapPx = with(density) { 4.dp.toPx() }
        val ink = DeskTheme.colors.onDeskSecondary
        val indicatorColor = MaterialTheme.colorScheme.primary
        val onIndicator = MaterialTheme.colorScheme.onPrimary

        Box(modifier = Modifier.width(LetterTouchWidth).height(extent * count)) {
            letters.forEachIndexed { index, letter ->
                val wave = { active.value * IndexScrubMath.waveFraction(index, state.focus, DeskMotion.LetterWaveRadius) }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset { IntOffset(x = -(waveShiftPx * wave()).roundToInt(), y = (index * extentPx).roundToInt()) }
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
                    Text(text = letter, style = MaterialTheme.typography.labelLarge, color = ink)
                }
            }

            // 원형 인디케이터: 파도 꼭대기 글자의 바로 왼쪽
            val focusedLetter = letters[state.focusedIndex.coerceIn(0, count - 1)]
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset {
                        IntOffset(
                            x = -(waveShiftPx * active.value + letterWidthPx + gapPx).roundToInt(),
                            y = (state.focus * extentPx + (extentPx - indicatorPx) / 2f).roundToInt(),
                        )
                    }
                    .graphicsLayer {
                        alpha = active.value
                        scaleX = 0.8f + 0.2f * active.value
                        scaleY = 0.8f + 0.2f * active.value
                    }
                    .size(DeskMotion.LetterIndicatorSize)
                    .background(indicatorColor, CircleShape)
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                Text(focusedLetter, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium, color = onIndicator)
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
private val LetterTouchWidth = 36.dp

/** 스크린샷 레퍼런스 재현: 22글자 중 M(12번째) 근처를 훑는 순간 */
@DeskPreviews
@Composable
private fun IndexLetterStripPreview() {
    DeskTheme {
        DeskSurface {
            IndexLetterStrip(
                letters = listOf("★", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "V", "W", "Y", "#"),
                state = rememberIndexScrubState(initialFocus = 13f, initialPhase = ScrubPhase.Scrubbing),
                onOpen = {},
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(0.7f),
            )
        }
    }
}
