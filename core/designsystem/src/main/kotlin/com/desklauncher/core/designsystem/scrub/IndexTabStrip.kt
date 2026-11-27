package com.desklauncher.core.designsystem.scrub

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.ui.AbsoluteAlignment
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
 * 서류철 인덱스 탭 스트립 (SHELL-03, PRD §3) — '툭툭' 튀어나오는 서류철.
 * [side] 가장자리에 붙어 있고, 손가락이 칸을 넘을 때마다 그 서류철이 스프링으로 화면 안쪽으로 툭 튀어나오며
 * (이웃은 조금) 지나간 서류철은 다시 들어간다. 넘김 THUD 진동과 같은 박자.
 * 선택 서류철 옆(화면 안쪽)에는 도구 이름 알약이 칸 단위로 따라간다.
 * 애니메이션 값은 offset/graphicsLayer 람다에서만 읽어 재구성을 피한다(칸이 바뀔 때만 재구성).
 *
 * @param side 서류철이 꽂히는 화면 가장자리
 * @param scrubSide 엄지로 훑는 영역이 있는 가장자리. [side]와 다르면 스트립이 화면 폭 전체를 쓰고,
 *   훑는 영역은 반대편 가장자리에서 [DeskMotion.ScrubZoneEdgeInset] 안쪽에 놓인다 (뒤로가기 제스처와 분리).
 *   이때 서류철 자체도 탭으로 열 수 있다.
 * @param openActionLabel TalkBack "두 번 탭하여 ○○" 문구
 */
@Composable
fun IndexTabStrip(
    items: List<IndexTabItem>,
    state: IndexScrubState,
    onOpen: (index: Int) -> Unit,
    openActionLabel: String,
    modifier: Modifier = Modifier,
    side: StripSide = StripSide.Right,
    scrubSide: StripSide = side,
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
    val edge = side.edgeAlignment()
    // 화면 안쪽 방향: 오른쪽 스트립은 -x(왼쪽), 왼쪽 스트립은 +x(오른쪽)
    val inward = if (side == StripSide.Right) -1f else 1f
    val split = scrubSide != side

    BoxWithConstraints(modifier = modifier.fillMaxHeight(), contentAlignment = side.centerAlignment()) {
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

        // 탭·터치 영역이 같은 상자(같은 세로 범위) 안에 있어야 훑는 높이와 튀어나오는 서류철이 맞는다
        val boxWidth = if (split) Modifier.fillMaxWidth() else Modifier.width(maxOf(tabWidth, DeskMotion.StripTouchWidth))
        Box(modifier = boxWidth.height(extent * count)) {
            items.forEachIndexed { index, item ->
                val pop = animateFloatAsState(
                    targetValue = if (scrubbing) IndexScrubMath.popLevel(index, focused) else 0f,
                    animationSpec = popSpring,
                    label = "tabPop",
                )
                IndexTab(
                    item = item,
                    side = side,
                    // Idle에서는 튀어나올 거리만큼 화면 밖으로 밀려 있다
                    x = { -inward * popShiftPx * (1f - pop.value * active.value) },
                    y = { index * extentPx },
                    openActionLabel = openActionLabel,
                    onOpen = { openWithHaptic(index) },
                    // 훑는 영역이 반대편이면 서류철은 가려지지 않으므로 직접 눌러 열 수 있다
                    tappable = split,
                    modifier = Modifier
                        .align(edge)
                        .width(tabWidth)
                        .height(extent - DeskMotion.TabGap),
                )
            }

            TabIndicator(
                item = items[focused],
                side = side,
                active = active,
                offset = {
                    IntOffset(
                        x = (inward * (idleVisiblePx + popShiftPx * active.value + indicatorGapPx(density))).roundToInt(),
                        y = (indicatorIndex.value * extentPx + (extentPx - indicatorHeightPx(density)) / 2f).roundToInt(),
                    )
                },
                modifier = Modifier.align(edge),
            )

            if (split) {
                ScrubZone(
                    state = state,
                    items = items,
                    active = active,
                    extentPx = extentPx,
                    modifier = Modifier
                        .align(scrubSide.edgeAlignment())
                        .then(
                            if (scrubSide == StripSide.Right) {
                                Modifier.absolutePadding(right = DeskMotion.ScrubZoneEdgeInset)
                            } else {
                                Modifier.absolutePadding(left = DeskMotion.ScrubZoneEdgeInset)
                            },
                        )
                        .width(DeskMotion.ScrubZoneWidth)
                        .fillMaxHeight()
                        .indexScrubGesture(
                            state = state,
                            itemExtentPx = extentPx,
                            cancelDistancePx = cancelPx,
                            onOpen = openWithHaptic,
                            side = scrubSide,
                        ),
                )
            } else {
                // 터치 영역: 탭 위에 겹친 투명 박스. 의미 정보는 각 탭이 가진다.
                Box(
                    modifier = Modifier
                        .align(edge)
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
                            side = side,
                        ),
                )
            }
        }
    }
}

/**
 * 훑는 영역의 눈금: 가는 세로선 + 서류철마다 점. 스크럽 중에는 현재 칸의 점이 그 서류철 색으로 커진다.
 * 값은 그리기 단계에서만 읽어 재구성을 피한다.
 */
@Composable
private fun ScrubZone(
    state: IndexScrubState,
    items: List<IndexTabItem>,
    active: State<Float>,
    extentPx: Float,
    modifier: Modifier = Modifier,
) {
    val line = DeskTheme.colors.lineStrong
    Box(
        modifier = modifier
            .clearAndSetSemantics { }
            .drawBehind {
                val x = size.width / 2f
                drawLine(line, Offset(x, extentPx / 2f), Offset(x, size.height - extentPx / 2f), strokeWidth = DeskLine.Hairline.toPx())
                val focused = state.focusedIndex.coerceIn(0, items.lastIndex)
                val dot = ScrubDotRadius.toPx()
                items.forEachIndexed { index, item ->
                    val center = Offset(x, index * extentPx + extentPx / 2f)
                    if (index == focused && active.value > 0f) {
                        drawCircle(item.color.container, radius = dot * (1f + 1.5f * active.value), center = center)
                    } else {
                        drawCircle(line, radius = dot, center = center)
                    }
                }
            },
    )
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

private fun StripSide.edgeAlignment(): Alignment =
    if (this == StripSide.Right) AbsoluteAlignment.TopRight else AbsoluteAlignment.TopLeft

private fun StripSide.centerAlignment(): Alignment =
    if (this == StripSide.Right) AbsoluteAlignment.CenterRight else AbsoluteAlignment.CenterLeft

private fun indicatorGapPx(density: Density) = with(density) { DeskMotion.IndicatorGap.toPx() }
private fun indicatorHeightPx(density: Density) = with(density) { DeskMotion.IndicatorHeight.toPx() }

@Composable
private fun IndexTab(
    item: IndexTabItem,
    side: StripSide,
    x: () -> Float,
    y: () -> Float,
    openActionLabel: String,
    onOpen: () -> Unit,
    tappable: Boolean,
    modifier: Modifier = Modifier,
) {
    // 둥근 모서리·아이콘은 화면 안쪽(보이는 쪽)에 둔다
    val shape = if (side == StripSide.Right) RightTabShape else LeftTabShape
    Box(
        modifier = modifier
            .absoluteOffset { IntOffset(x = x().roundToInt(), y = y().roundToInt()) }
            .background(item.color.container, shape)
            // 그림자 대신 얇은 선으로 탭끼리 구분
            .border(DeskLine.Hairline, DeskTheme.colors.lineStrong, shape)
            .then(
                if (tappable) {
                    Modifier
                        .clip(shape)
                        .clickable(onClickLabel = openActionLabel, role = Role.Button, onClick = onOpen)
                        .semantics { contentDescription = item.label }
                } else {
                    Modifier.semantics {
                        role = Role.Button
                        contentDescription = item.label
                        onClick(label = openActionLabel) {
                            onOpen()
                            true
                        }
                    }
                },
            ),
        contentAlignment = if (side == StripSide.Right) AbsoluteAlignment.CenterLeft else AbsoluteAlignment.CenterRight,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.color.onContainer,
            modifier = Modifier.absolutePadding(left = TabIconInset, right = TabIconInset).size(18.dp),
        )
    }
}

/** 선택 탭 옆(화면 안쪽) 알약: 탭 색 + 아이콘 + 도구 이름. 칸 단위로 따라간다. */
@Composable
private fun TabIndicator(
    item: IndexTabItem,
    side: StripSide,
    active: State<Float>,
    offset: Density.() -> IntOffset,
    modifier: Modifier = Modifier,
) {
    val anchoredRight = side == StripSide.Right
    Row(
        modifier = modifier
            .absoluteOffset(offset)
            .graphicsLayer {
                alpha = active.value
                scaleX = 0.9f + 0.1f * active.value
                scaleY = 0.9f + 0.1f * active.value
                // 탭 쪽 끝을 기준으로 커진다
                transformOrigin = TransformOrigin(if (anchoredRight) 1f else 0f, 0.5f)
            }
            // 스트립 상자 폭(약 96dp)에 갇혀 이름이 잘리지 않도록, 탭 쪽 끝에 붙인 채 화면 안쪽으로 넘쳐 그린다
            .wrapContentWidth(if (anchoredRight) AbsoluteAlignment.Right else AbsoluteAlignment.Left, unbounded = true)
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

private val TabIconInset = 11.dp
private val ScrubDotRadius = 2.dp
private val RightTabShape = AbsoluteRoundedCornerShape(topLeft = 10.dp, bottomLeft = 10.dp)
private val LeftTabShape = AbsoluteRoundedCornerShape(topRight = 10.dp, bottomRight = 10.dp)
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
private fun IndexTabStripLeftIdlePreview() {
    DeskTheme {
        DeskSurface {
            IndexTabStrip(
                items = PreviewItems,
                state = rememberIndexScrubState(),
                onOpen = {},
                openActionLabel = "열기",
                modifier = Modifier.align(AbsoluteAlignment.CenterLeft),
                side = StripSide.Left,
            )
        }
    }
}

/** 스크럽 중 정지 스냅샷(왼쪽 가장자리): 네 번째 서류철이 오른쪽으로 튀어나온 상태와 알약 인디케이터. */
@DeskPreviews
@Composable
private fun IndexTabStripLeftScrubbingPreview() {
    DeskTheme {
        DeskSurface {
            IndexTabStrip(
                items = PreviewItems,
                state = rememberIndexScrubState(initialFocus = 3.3f, initialPhase = ScrubPhase.Scrubbing),
                onOpen = {},
                openActionLabel = "열기",
                modifier = Modifier.align(AbsoluteAlignment.CenterLeft),
                side = StripSide.Left,
            )
        }
    }
}

/** 책상 배치 스냅샷: 서류철은 왼쪽, 엄지는 오른쪽 눈금을 훑는 중 (네 번째 칸) */
@DeskPreviews
@Composable
private fun IndexTabStripSplitScrubbingPreview() {
    DeskTheme {
        DeskSurface {
            IndexTabStrip(
                items = PreviewItems,
                state = rememberIndexScrubState(initialFocus = 3.3f, initialPhase = ScrubPhase.Scrubbing),
                onOpen = {},
                openActionLabel = "열기",
                modifier = Modifier.align(Alignment.Center),
                side = StripSide.Left,
                scrubSide = StripSide.Right,
            )
        }
    }
}

/** 스크럽 중 정지 스냅샷(오른쪽 가장자리) */
@DeskPreviews
@Composable
private fun IndexTabStripRightScrubbingPreview() {
    DeskTheme {
        DeskSurface {
            IndexTabStrip(
                items = PreviewItems,
                state = rememberIndexScrubState(initialFocus = 3.3f, initialPhase = ScrubPhase.Scrubbing),
                onOpen = {},
                openActionLabel = "열기",
                modifier = Modifier.align(AbsoluteAlignment.CenterRight),
                side = StripSide.Right,
            )
        }
    }
}
