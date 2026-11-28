package com.desklauncher.feature.desk.strip

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable

import androidx.compose.runtime.getValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.motion.LocalReducedMotion
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskIndexColor
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.desk.R
import kotlin.math.roundToInt

/** 서류철의 보이는 상태 (제스처 판정은 [com.desklauncher.feature.desk.strip.ScrubTracker]) */
@Immutable
data class StripVisualState(
    val focused: Int? = null,
    val exploring: Boolean = false,
    val cancelArmed: Boolean = false,
) {
    companion object {
        val Idle = StripVisualState()
    }
}

/** 서류철 치수 시작값 [실험] (PRD §4.1, §17) */
object StripDimens {
    /** 칸 간격 — 약 9mm */
    val Extent: Dp = 56.dp
    /** 구분 여백 */
    val Gap: Dp = 16.dp
    /** 왼쪽 책등 시각 폭 */
    val SpineVisible: Dp = 40.dp
    /** 왼쪽 책등이 튀어나오는 거리 — 숨어 있던 부분이 드러난다 */
    val SpinePop: Dp = 48.dp
    /** 오른쪽 표시 비교군의 탭 폭 — 보이는 탭 = 누르는 곳 */
    val RightTabWidth: Dp = 56.dp
    val RightTabPop: Dp = 20.dp
    /** 칸과 칸 사이 시각 틈 */
    val SlotInnerGap: Dp = 3.dp
    /** 이름표와 탭 사이 */
    val LabelGap: Dp = 8.dp
    val LabelHeight: Dp = 36.dp
    /** 분리형 훑기 영역 폭 — 엄지 폭 약 1.5배 [실험] (PRD §4.1) */
    val ScrubZoneWidth: Dp = 64.dp
    /** 훑기 중 본문 쪽으로 이만큼 빼면 취소 대기 */
    val CancelDistance: Dp = 64.dp
}

/**
 * 서류철 칸들 (STRIP-01). 높이는 [geometry]가 정하고, 이 컴포저블은 그리기와 직접 탭만 맡는다.
 * 훑기 판정은 부모(책상)가 같은 [geometry]로 한다 (PRD §4.2 구현 규칙).
 *
 * @param edgeInset [StripSide.Right]일 때 오른쪽 가장자리에서 띄우는 거리(시스템 제스처 인셋 + 여유)
 */
@Composable
fun DeskStrip(
    slots: List<StripSlot>,
    geometry: StripGeometry,
    state: StripVisualState,
    onSlotClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    side: StripSide = StripSide.Left,
    edgeInset: Dp = 0.dp,
) {
    val density = LocalDensity.current
    val heightDp = with(density) { geometry.height.toDp() }
    val reduced = LocalReducedMotion.current
    val active by animateFloatAsState(
        targetValue = when {
            !state.exploring -> 0f
            state.cancelArmed -> 0.35f
            else -> 1f
        },
        animationSpec = if (reduced) snap() else tween(DeskMotion.RevealMillis, easing = DeskMotion.EaseOut),
        label = "stripActive",
    )

    Box(modifier = modifier.fillMaxWidth().height(heightDp)) {
        slots.forEachIndexed { index, slot ->
            val pop = animateFloatAsState(
                targetValue = if (state.exploring) geometry.popLevel(index, state.focused) else 0f,
                animationSpec = if (reduced) snap() else DeskMotion.tabPop(),
                label = "slotPop",
            )
            SpineTab(
                slot = slot,
                index = index,
                side = side,
                pop = { pop.value * active },
                topPx = geometry.top(index),
                extent = with(density) { geometry.extent.toDp() },
                edgeInset = edgeInset,
                onClick = { onSlotClick(index) },
            )
        }

        val focused = state.focused
        if (focused != null && focused in slots.indices) {
            val labelY by animateFloatAsState(
                targetValue = geometry.center(focused),
                animationSpec = if (reduced) snap() else DeskMotion.glide(),
                label = "labelY",
            )
            NameLabel(
                text = if (state.cancelArmed) stringResource(R.string.slot_cancel) else slotName(slots[focused], focused),
                side = side,
                edgeInset = edgeInset,
                active = { active },
                centerY = { labelY },
            )
        }
    }
}

@Composable
private fun BoxScope.SpineTab(
    slot: StripSlot,
    index: Int,
    side: StripSide,
    pop: () -> Float,
    topPx: Float,
    extent: Dp,
    edgeInset: Dp,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val colors = DeskTheme.colors
    val left = side == StripSide.Left
    val visible = if (left) StripDimens.SpineVisible else StripDimens.RightTabWidth
    val popShift = if (left) StripDimens.SpinePop else StripDimens.RightTabPop
    val popShiftPx = with(density) { popShift.toPx() }
    val shape = if (left) {
        AbsoluteRoundedCornerShape(topRight = 10.dp, bottomRight = 10.dp)
    } else {
        AbsoluteRoundedCornerShape(topLeft = 10.dp, bottomLeft = 10.dp)
    }
    val fill = when {
        slot.isEmptyNotebook -> colors.desk
        slot.color != null -> slot.color.container
        else -> colors.paper
    }
    val ink = if (slot.color != null) slot.color.onContainer else colors.onPaper
    val name = slotName(slot, index)
    val description = if (slot.kind == SlotKind.Notebook) {
        stringResource(R.string.slot_pinned_description, index + 1, name)
    } else {
        name
    }
    val openLabel = stringResource(R.string.slot_open_action)

    Box(
        modifier = Modifier
            .then(if (left) Modifier else Modifier.align(AbsoluteAlignment.TopRight).absolutePadding(right = edgeInset))
            .absoluteOffset {
                // 왼쪽: 튀어나올 거리만큼 화면 밖에 숨어 있다가 오른쪽으로 나온다. 오른쪽 비교군: 왼쪽으로 살짝 나온다.
                val x = if (left) -popShiftPx * (1f - pop()) else -popShiftPx * pop()
                IntOffset(x.roundToInt(), topPx.roundToInt())
            }
            .width(if (left) visible + popShift else visible)
            .height(extent - StripDimens.SlotInnerGap)
            .clip(shape)
            .background(fill)
            .border(DeskLine.hairline, colors.lineStrong, shape)
            .clickable(onClickLabel = openLabel, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Box(
            modifier = Modifier
                .align(if (left) AbsoluteAlignment.CenterRight else AbsoluteAlignment.CenterLeft)
                .width(visible)
                .fillMaxHeight()
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = spineText(slot, index),
                style = DeskType.spine.copy(fontSize = with(density) { 12.dp.toSp() }),
                color = if (slot.isEmptyNotebook) colors.muted else ink,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** 탐색 중 튀어나온 칸 옆(화면 안쪽)의 전체 이름표 — 칸 단위로 미끄러진다 */
@Composable
private fun BoxScope.NameLabel(
    text: String,
    side: StripSide,
    edgeInset: Dp,
    active: () -> Float,
    centerY: () -> Float,
) {
    val density = LocalDensity.current
    val colors = DeskTheme.colors
    val left = side == StripSide.Left
    val reduced = LocalReducedMotion.current
    val startPx = with(density) {
        if (left) (StripDimens.SpineVisible + StripDimens.SpinePop + StripDimens.LabelGap).toPx()
        else (edgeInset + StripDimens.RightTabWidth + StripDimens.RightTabPop + StripDimens.LabelGap).toPx()
    }
    val labelHeightPx = with(density) { StripDimens.LabelHeight.toPx() }
    Box(
        modifier = Modifier
            .align(if (left) AbsoluteAlignment.TopLeft else AbsoluteAlignment.TopRight)
            .absoluteOffset {
                val x = if (left) startPx else -startPx
                IntOffset(x.roundToInt(), (centerY() - labelHeightPx / 2f).roundToInt())
            }
            .graphicsLayer { alpha = active() }
            .wrapContentWidth(if (left) Alignment.Start else Alignment.End, unbounded = true)
            .height(StripDimens.LabelHeight)
            .background(colors.paper, DeskShape.pill)
            .border(DeskLine.hairline, colors.lineStrong, DeskShape.pill)
            .padding(horizontal = 14.dp)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        // 이름이 바뀔 때는 beUI '내용 교체'(나감 120ms 위로 4dp, 들어옴 180ms 아래에서 4dp)
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                if (reduced) {
                    fadeIn(tween(DeskMotion.ReducedFadeMillis)) togetherWith fadeOut(tween(DeskMotion.ReducedFadeMillis))
                } else {
                    val shift = with(density) { DeskMotion.SwapOffset.roundToPx() }
                    (slideInVertically(tween(DeskMotion.SwapInMillis, easing = DeskMotion.EaseOut)) { shift } +
                        fadeIn(tween(DeskMotion.SwapInMillis))) togetherWith
                        (slideOutVertically(tween(DeskMotion.SwapOutMillis)) { -shift } + fadeOut(tween(DeskMotion.SwapOutMillis)))
                }
            },
            label = "labelSwap",
        ) { label ->
            Text(text = label, style = DeskType.label, color = colors.onPaper, maxLines = 1)
        }
    }
}

@Composable
internal fun slotName(slot: StripSlot, index: Int): String = when (slot.kind) {
    SlotKind.Notebook -> slot.notebookName ?: stringResource(R.string.slot_empty_name, index + 1)
    SlotKind.All -> stringResource(R.string.slot_all_name)
    SlotKind.Tools -> stringResource(R.string.slot_tools_name)
    SlotKind.Apps -> stringResource(R.string.slot_apps_name)
}

@Composable
private fun spineText(slot: StripSlot, index: Int): String = when (slot.kind) {
    // 책등은 별칭 앞 2자만 (PRD §4.1). 빈 자리는 번호만.
    SlotKind.Notebook -> slot.alias?.take(2) ?: slot.notebookName?.take(2) ?: (index + 1).toString()
    SlotKind.All -> stringResource(R.string.slot_all_alias)
    SlotKind.Tools -> stringResource(R.string.slot_tools_alias)
    SlotKind.Apps -> stringResource(R.string.slot_apps_alias)
}

// ── Preview ────────────────────────────────────────────────

private val PreviewSlots = DefaultStrip.slots(
    pinned = listOf(
        StripSlot("n1", SlotKind.Notebook, notebookName = "보드 검증", alias = "보드", color = DeskIndexColor.Apricot),
        StripSlot("n2", SlotKind.Notebook, notebookName = "앱 개발", alias = "개발", color = DeskIndexColor.Sage),
        null,
    ),
)

@Composable
private fun PreviewStrip(state: StripVisualState, side: StripSide = StripSide.Left) {
    val density = LocalDensity.current
    val geometry = with(density) {
        StripGeometry(StripDimens.Extent.toPx(), StripDimens.Gap.toPx(), PreviewSlots.size, DefaultStrip.GapBeforeIndex)
    }
    DeskTheme {
        Box(Modifier.fillMaxSize().background(DeskTheme.colors.desk)) {
            DeskStrip(
                slots = PreviewSlots,
                geometry = geometry,
                state = state,
                onSlotClick = {},
                side = side,
                edgeInset = if (side == StripSide.Right) 32.dp else 0.dp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
            )
        }
    }
}

@DeskPreviews
@Composable
private fun DeskStripIdlePreview() = PreviewStrip(StripVisualState.Idle)

/** 정지 스냅샷: 두 번째 공책(개발)에 엄지가 머문 상태 */
@DeskPreviews
@Composable
private fun DeskStripExploringPreview() = PreviewStrip(StripVisualState(focused = 1, exploring = true))

/** 정지 스냅샷: 탐색 중 본문 쪽으로 빼서 취소 대기 */
@DeskPreviews
@Composable
private fun DeskStripCancelPreview() = PreviewStrip(StripVisualState(focused = 4, exploring = true, cancelArmed = true))

/** 비교군(STRIP-04): 오른쪽 표시 */
@DeskPreviews
@Composable
private fun DeskStripRightVisiblePreview() = PreviewStrip(StripVisualState(focused = 3, exploring = true), StripSide.Right)
