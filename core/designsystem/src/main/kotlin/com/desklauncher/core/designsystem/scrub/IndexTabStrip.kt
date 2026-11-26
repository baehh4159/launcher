package com.desklauncher.core.designsystem.scrub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.component.DeskSurface
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import kotlin.math.min
import kotlin.math.roundToInt

@Immutable
data class IndexTabItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: DeskTabColor,
)

/**
 * 우측 서류철 인덱스 탭 스트립 (SHELL-03, PRD §3).
 * 부모가 준 세로 공간 안에서 탭 간격을 [DeskMotion.TabMinExtent]~[DeskMotion.TabMaxExtent]로 맞추고 가운데 정렬한다.
 * 프레임마다 바뀌는 값(focus, pop)은 offset/graphicsLayer 람다에서만 읽어 재구성을 피한다.
 *
 * @param openActionLabel TalkBack "두 번 탭하여 ○○" 문구
 */
@Composable
fun IndexTabStrip(
    items: List<IndexTabItem>,
    state: IndexScrubState,
    onOpen: (index: Int) -> Unit,
    openActionLabel: String,
    modifier: Modifier = Modifier,
) {
    val count = items.size
    SideEffect { state.itemCount = count }
    if (count == 0) return

    val haptics = rememberDeskHaptics()
    IndexScrubHaptics(state, haptics)
    val currentOnOpen by rememberUpdatedState(onOpen)
    val openWithHaptic: (Int) -> Unit = remember(haptics) {
        { index ->
            haptics.confirm()
            currentOnOpen(index)
        }
    }

    val active = animateFloatAsState(
        targetValue = when {
            state.phase == ScrubPhase.Idle -> 0f
            state.isCancelArmed -> 0.35f
            else -> 1f
        },
        animationSpec = tween(DeskMotion.PhaseFadeMillis),
        label = "stripActive",
    )

    BoxWithConstraints(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
        val extent = (maxHeight / count).coerceIn(DeskMotion.TabMinExtent, DeskMotion.TabMaxExtent)
        val density = LocalDensity.current
        val extentPx = with(density) { extent.toPx() }
        val maxPopPx = with(density) { DeskMotion.TabMaxPop.toPx() }
        val cancelPx = with(density) { DeskMotion.CancelDistance.toPx() }
        val exclusionPx = with(density) { DeskMotion.GestureExclusionMaxHeight.toPx() }

        Box(
            modifier = Modifier
                .width(DeskMotion.TabIdleVisibleWidth + DeskMotion.TabMaxPop)
                .height(extent * count),
        ) {
            items.forEachIndexed { index, item ->
                IndexTab(
                    item = item,
                    index = index,
                    state = state,
                    active = active,
                    extentPx = extentPx,
                    maxPopPx = maxPopPx,
                    openActionLabel = openActionLabel,
                    onOpen = openWithHaptic,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(DeskMotion.TabIdleVisibleWidth + DeskMotion.TabMaxPop)
                        .height(extent - DeskMotion.TabGap),
                )
            }

            // 터치 영역: 탭 위에 겹친 투명 박스. 의미 정보는 각 탭이 가진다.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(DeskMotion.StripTouchWidth)
                    .fillMaxHeight()
                    // 뒤로가기 제스처와 겹치는 가장자리 중 정책 한도(200dp)만큼만 가운데를 제외한다.
                    .systemGestureExclusion { coordinates ->
                        val height = coordinates.size.height.toFloat()
                        val excluded = min(height, exclusionPx)
                        val top = (height - excluded) / 2f
                        Rect(0f, top, coordinates.size.width.toFloat(), top + excluded)
                    }
                    .indexScrubGesture(
                        state = state,
                        itemExtentPx = extentPx,
                        cancelDistancePx = cancelPx,
                        onOpen = openWithHaptic,
                    ),
            )
        }
    }
}

@Composable
private fun IndexTab(
    item: IndexTabItem,
    index: Int,
    state: IndexScrubState,
    active: State<Float>,
    extentPx: Float,
    maxPopPx: Float,
    openActionLabel: String,
    onOpen: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pop = { active.value * IndexScrubMath.popFraction(index, state.focus) }
    val shape = TabShape
    Box(
        modifier = modifier
            .offset {
                val spread = active.value * IndexScrubMath.spreadOffset(index, state.focus)
                IntOffset(
                    x = (maxPopPx * (1f - pop())).roundToInt(),
                    y = ((index + spread) * extentPx).roundToInt(),
                )
            }
            .graphicsLayer {
                val p = pop()
                shadowElevation = (1f + 7f * p).dp.toPx()
                this.shape = shape
                clip = true
                scaleY = 1f + 0.12f * p
                transformOrigin = TransformOrigin(1f, 0.5f)
            }
            .background(item.color.container, shape)
            .semantics {
                role = Role.Button
                contentDescription = item.label
                onClick(label = openActionLabel) {
                    onOpen(index)
                    true
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 7.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.color.onContainer,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = item.label,
                color = item.color.onContainer,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer { alpha = pop() },
            )
        }
    }
}

private val TabShape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)

private val PreviewItems = listOf(
    IndexTabItem("todo", "할일정리", Icons.Outlined.Checklist, DeskTabColor.Orange),
    IndexTabItem("calendar", "캘린더", Icons.Outlined.CalendarMonth, DeskTabColor.Red),
    IndexTabItem("cabinet", "서류정리함", Icons.Outlined.FolderOpen, DeskTabColor.Brown),
    IndexTabItem("calculator", "계산기", Icons.Outlined.Calculate, DeskTabColor.Teal),
    IndexTabItem("alarm", "알람", Icons.Outlined.Alarm, DeskTabColor.Indigo),
)

@DeskPreviews
@Composable
private fun IndexTabStripIdlePreview() {
    DeskTheme {
        DeskSurface {
            IndexTabStrip(
                items = PreviewItems,
                state = rememberIndexScrubState(),
                onOpen = {},
                openActionLabel = "열기",
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

/** 스크럽 중 정지 스냅샷: 포커스 2.4 (서류정리함과 계산기 사이). */
@DeskPreviews
@Composable
private fun IndexTabStripScrubbingPreview() {
    DeskTheme {
        DeskSurface {
            IndexTabStrip(
                items = PreviewItems,
                state = rememberIndexScrubState(initialFocus = 2.4f, initialPhase = ScrubPhase.Scrubbing),
                onOpen = {},
                openActionLabel = "열기",
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}
