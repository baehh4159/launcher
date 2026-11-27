package com.desklauncher.core.designsystem.scrub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlin.math.roundToInt

/**
 * 앱 서랍 초성/알파벳 인덱스 스트립 (SHELL-07) — 서류철 스트립과 같은 파도 스크럽.
 * 글자들이 손가락 주변에서 넓은 곡선을 그리며 왼쪽으로 휘고, 휜 글자 왼쪽에 원형 인디케이터가 따라온다.
 * 포커스가 바뀔 때 목록을 옮기는 일은 호출부가 [IndexScrubState.focusedIndex]를 보고 처리한다.
 *
 * 오른쪽 가장자리에 붙이되 글자와 터치 영역은 [DeskMotion.LetterEdgeInset]만큼 안쪽에서 시작한다.
 * 가장자리 띠는 시스템 뒤로가기 스와이프 몫으로 비워 두므로 제스처 제외(systemGestureExclusion)도 하지 않는다.
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

    BoxWithConstraints(modifier = modifier.fillMaxHeight(), contentAlignment = AbsoluteAlignment.CenterRight) {
        val extent = (maxHeight / count).coerceIn(LetterMinExtent, LetterMaxExtent)
        val density = LocalDensity.current
        val extentPx = with(density) { extent.toPx() }
        val waveShiftPx = with(density) { DeskMotion.LetterWaveShift.toPx() }
        val cancelPx = with(density) { DeskMotion.CancelDistance.toPx() }
        val insetPx = with(density) { DeskMotion.LetterEdgeInset.toPx() }
        val columnPx = with(density) { DeskMotion.LetterColumnWidth.toPx() }
        val indicatorPx = with(density) { DeskMotion.LetterIndicatorSize.toPx() }
        val gapPx = with(density) { 4.dp.toPx() }
        val ink = DeskTheme.colors.onDeskSecondary
        val indicatorColor = MaterialTheme.colorScheme.primary
        val onIndicator = MaterialTheme.colorScheme.onPrimary
        val touchWidth = DeskMotion.LetterColumnWidth + DeskMotion.LetterTouchExtra

        Box(modifier = Modifier.width(DeskMotion.LetterEdgeInset + touchWidth).height(extent * count)) {
            letters.forEachIndexed { index, letter ->
                val wave = { active.value * IndexScrubMath.waveFraction(index, state.focus, DeskMotion.LetterWaveRadius) }
                Box(
                    modifier = Modifier
                        .align(AbsoluteAlignment.TopRight)
                        .absoluteOffset {
                            IntOffset(x = -(insetPx + waveShiftPx * wave()).roundToInt(), y = (index * extentPx).roundToInt())
                        }
                        .size(width = DeskMotion.LetterColumnWidth, height = extent)
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
                    .align(AbsoluteAlignment.TopRight)
                    .absoluteOffset {
                        IntOffset(
                            x = -(insetPx + waveShiftPx * active.value + columnPx + gapPx).roundToInt(),
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

            // 터치 영역: 가장자리 띠를 건너뛰고, 글자 열 + 화면 안쪽 여유까지
            Box(
                modifier = Modifier
                    .align(AbsoluteAlignment.TopRight)
                    .absolutePadding(right = DeskMotion.LetterEdgeInset)
                    .width(touchWidth)
                    .fillMaxHeight()
                    .indexScrubGesture(state, extentPx, cancelPx, onOpen),
            )
        }
    }
}

private val LetterMinExtent = 14.dp
private val LetterMaxExtent = 26.dp

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
