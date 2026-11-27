package com.desklauncher.core.designsystem.scrub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.component.DeskSurface
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskLine
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
 * 우측 서류철 인덱스 탭 스트립 (SHELL-03, PRD §3) — '툭툭' 튀어나오는 서류철.
 * 손가락이 칸을 넘을 때마다 그 서류철이 스프링으로 왼쪽으로 툭 튀어나오고(이웃은 조금),
 * 지나간 서류철은 다시 들어간다. 넘김 THUD 진동과 같은 박자.
 * 선택 서류철 왼쪽에는 도구 이름 알약이 칸 단위로 따라간다.
 * 애니메이션 값은 offset/graphicsLayer 람다에서만 읽어 재구성을 피한다(칸이 바뀔 때만 재구성).
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
    val active = rememberStripActive(state)

    BoxWithConstraints(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
        val extent = (maxHeight / count).coerceIn(DeskMotion.TabMinExtent, DeskMotion.TabMaxExtent)
        val density = LocalDensity.current
        val extentPx = with(density) { extent.toPx() }
        val popShiftPx = with(density) { DeskMotion.TabPopShift.toPx() }
        val idleVisiblePx = with(density) { DeskMotion.TabIdleVisibleWidth.toPx() }
        val cancelPx = with(density) { DeskMotion.CancelDistance.toPx() }
        val exclusionPx = with(density) { DeskMotion.GestureExclusionMaxHeight.toPx() }
        val tabWidth = DeskMotion.TabIdleVisibleWidth + DeskMotion.TabPopShift
        val focused = state.focusedIndex.coerceIn(0, count - 1)
        val scrubbing = state.phase != ScrubPhase.Idle
        val popSpring = spring<Float>(dampingRatio = DeskMotion.TabPopDamping, stiffness = DeskMotion.TabPopStiffness)
        // 알약은 손가락을 연속으로 따르지 않고 칸 단위로 '툭' 옮겨 간다
        val indicatorIndex = animateFloatAsState(focused.toFloat(), popSpring, label = "indicatorIndex")

        Box(modifier = Modifier.width(tabWidth).height(extent * count)) {
            items.forEachIndexed { index, item ->
                val pop = animateFloatAsState(
                    targetValue = if (scrubbing) IndexScrubMath.popLevel(index, focused) else 0f,
                    animationSpec = popSpring,
                    label = "tabPop",
                )
                IndexTab(
                    item = item,
                    pop = { pop.value * active.value },
                    y = { index * extentPx },
                    popShiftPx = popShiftPx,
                    openActionLabel = openActionLabel,
                    onOpen = { openWithHaptic(index) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .width(tabWidth)
                        .height(extent - DeskMotion.TabGap),
                )
            }

            TabIndicator(
                item = items[focused],
                active = active,
                offset = {
                    IntOffset(
                        x = -(idleVisiblePx + popShiftPx * active.value + indicatorGapPx(density)).roundToInt(),
                        y = (indicatorIndex.value * extentPx + (extentPx - indicatorHeightPx(density)) / 2f).roundToInt(),
                    )
                },
                modifier = Modifier.align(Alignment.TopEnd),
            )

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

/** Idle 0 → 스크럽 1 (취소 대기 중에는 0.35로 움츠러듦) */
@Composable
internal fun rememberStripActive(state: IndexScrubState): State<Float> = animateFloatAsState(
    targetValue = when {
        state.phase == ScrubPhase.Idle -> 0f
        state.isCancelArmed -> 0.35f
        else -> 1f
    },
    animationSpec = tween(DeskMotion.PhaseFadeMillis),
    label = "stripActive",
)

private fun indicatorGapPx(density: Density) = with(density) { DeskMotion.IndicatorGap.toPx() }
private fun indicatorHeightPx(density: Density) = with(density) { DeskMotion.IndicatorHeight.toPx() }

@Composable
private fun IndexTab(
    item: IndexTabItem,
    pop: () -> Float,
    y: () -> Float,
    popShiftPx: Float,
    openActionLabel: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .offset { IntOffset(x = (popShiftPx * (1f - pop())).roundToInt(), y = y().roundToInt()) }
            .background(item.color.container, TabShape)
            // 그림자 대신 얇은 선으로 탭끼리 구분
            .border(DeskLine.Hairline, DeskTheme.colors.lineStrong, TabShape)
            .semantics {
                role = Role.Button
                contentDescription = item.label
                onClick(label = openActionLabel) {
                    onOpen()
                    true
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.color.onContainer,
            modifier = Modifier.padding(start = 7.dp).size(18.dp),
        )
    }
}

/** 선택 탭 왼쪽 알약: 탭 색 + 아이콘 + 도구 이름. 손가락 높이를 연속으로 따라간다. */
@Composable
private fun TabIndicator(
    item: IndexTabItem,
    active: State<Float>,
    offset: Density.() -> IntOffset,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .offset(offset)
            .graphicsLayer {
                alpha = active.value
                scaleX = 0.9f + 0.1f * active.value
                scaleY = 0.9f + 0.1f * active.value
                transformOrigin = TransformOrigin(1f, 0.5f)
            }
            .height(DeskMotion.IndicatorHeight)
            .background(item.color.container, PillShape)
            .border(DeskLine.Hairline, DeskTheme.colors.lineStrong, PillShape)
            .padding(horizontal = 14.dp)
            // 탭이 이미 의미 정보를 가지므로 인디케이터는 낭독하지 않는다
            .clearAndSetSemantics { },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(item.icon, contentDescription = null, tint = item.color.onContainer, modifier = Modifier.size(18.dp))
        Text(
            text = item.label,
            color = item.color.onContainer,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val TabShape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
private val PillShape = RoundedCornerShape(percent = 50)

private val PreviewItems = listOf(
    IndexTabItem("todo", "할일정리", Icons.Outlined.Checklist, DeskTabColor.Orange),
    IndexTabItem("calendar", "캘린더", Icons.Outlined.CalendarMonth, DeskTabColor.Red),
    IndexTabItem("cabinet", "서류정리함", Icons.Outlined.FolderOpen, DeskTabColor.Brown),
    IndexTabItem("calculator", "계산기", Icons.Outlined.Calculate, DeskTabColor.Teal),
    IndexTabItem("alarm", "알람", Icons.Outlined.Alarm, DeskTabColor.Indigo),
    IndexTabItem("todo2", "할일정리", Icons.Outlined.Checklist, DeskTabColor.Green),
    IndexTabItem("calendar2", "캘린더", Icons.Outlined.CalendarMonth, DeskTabColor.Blue),
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

/** 스크럽 중 정지 스냅샷: 네 번째 서류철이 튀어나온 상태와 알약 인디케이터. */
@DeskPreviews
@Composable
private fun IndexTabStripScrubbingPreview() {
    DeskTheme {
        DeskSurface {
            IndexTabStrip(
                items = PreviewItems,
                state = rememberIndexScrubState(initialFocus = 3.3f, initialPhase = ScrubPhase.Scrubbing),
                onOpen = {},
                openActionLabel = "열기",
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}
